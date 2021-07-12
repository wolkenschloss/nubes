package wolkenschloss.status;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.libvirt.DomainInfo;

import wolkenschloss.pool.PoolOperations;
import wolkenschloss.domain.DomainOperations;
import wolkenschloss.domain.RegistryService;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Status extends DefaultTask {

    @Internal
    abstract public Property<String> getDomainName();

    @Internal
    abstract public  DirectoryProperty getDownloadDir();

    @Internal
    abstract public  RegularFileProperty getBaseImageFile();

    @Internal
    abstract public RegularFileProperty getKnownHostsFile();

    @Internal
    abstract public RegularFileProperty getKubeConfigFile();

    @Inject
    abstract public ExecOperations getExecOperations();

    @Internal
    abstract public Property<PoolOperations> getPoolOperations();

    @Internal
    abstract public Property<DomainOperations> getDomainOperations();

    @TaskAction
    public void printStatus() {
        getLogger().quiet("Status of {}", getDomainName().get());

        DomainOperations domainOperations = getDomainOperations().get();

        check("Testbed", domainOperations::withDomain, (Consumer<DomainOperations>) domain -> {

            info("IP Address", domain::getTestbedHostAddress);

            check("Secure Shell", domain.withShell(getExecOperations()), shell -> {
                check("ssh uname", shell.withCommand("uname", "-norm"), result -> {
                    info("stdout", result::getStdout);
                    info("exit code", result::getExitValue);
                });
            });

            check("Connect", domain::withInfo, info -> {
                check("State", info.state, s -> s == DomainInfo.DomainState.VIR_DOMAIN_RUNNING);
                check("Memory (MB)", info.memory / 1024, m -> m >= 4096);
                check("Virtual CPU's", info.nrVirtCpu, n -> n > 1);
            });

                evaluate2("K8s config", () -> getKubeConfigFile().getAsFile().get().toPath(),
                        status -> status.check(Files::exists)
                                .ok(Path::toString)
                                .error("missing"));

                var poolOperations = getPoolOperations().get();
                check("Pool", poolOperations::run, p -> {
                    info("Pool Name", p::getName);
                    info("Pool Autostart", p::getAutostart);
                    info("Pool isActive", p::isActive);
                    evaluate2("Pool Volumes", p::listVolumes,
                            status -> status.check(vols -> Arrays.asList(vols).contains("root.qcow2") && Arrays.asList(vols).contains("cidata.img"))
                                    .ok(vols -> String.join(", ", vols))
                                    .error("Nicht genau drei Volumes")
                    );
                });

                RegistryService registryService = domain.getRegistry();
                check("Registry", registryService::withRegistry, (RegistryService registry) -> {
                    info("Address", registry::getAddress);
                    info("Upload Image", () -> registry.uploadImage("hello-world:latest"));
                    evaluate2("Catalogs", registry::listCatalogs,
                            status -> status.check(catalogs -> catalogs.contains("hello-world"))
                                    .ok(catalogs -> String.join(", ", catalogs))
                                    .error("missing catalog hello-world")
                    );
                });
            });

            info("XDG_DATA_HOME", () -> getDownloadDir().get().getAsFile().toPath());
            evaluate2("Base image", () -> getBaseImageFile().getAsFile().get().toPath(),
                    status -> status.check(Files::exists)
                            .ok(Path::toString)
                            .error("missing"));

    }

    private <T> void check(String label, T value, Predicate<T> requiredCondition) {

        if (requiredCondition.test(value)) {
            getLogger().quiet(String.format("✓ %-15s: %s", label, value));
        } else {
            getLogger().error(String.format("✗ %-15s: %s", label, value));
        }
    }

    private <T> void evaluate2(String label, CheckedSupplier<T> fn, Function<Check<T>, StatusChecker> check) {
        try {
            check.apply(new StatusBuilder<>(this, fn)).run(label);
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }

    private <T> void info(String label, CheckedSupplier<T> fn) {
        try {
            getLogger().quiet(String.format("✓ %-15s: %s", label, fn.apply()));
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }

    private <T> void check(String label, Consumer<Consumer<T>> fn, Consumer<T> with) {
        try {
            fn.accept(with);
            getLogger().quiet(String.format("✓ %-15s: %s", label, "OK"));
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }
}
