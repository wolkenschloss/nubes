package wolkenschloss.task.status;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.process.ExecOperations;
import org.libvirt.DomainInfo;
import wolkenschloss.*;
import wolkenschloss.task.CheckedConsumer;
import wolkenschloss.task.CopyKubeConfig;
import wolkenschloss.task.start.Start;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class StatusTask extends DefaultTask {

    @Internal
    abstract public Property<String> getDomainName();

    @Internal
    public abstract Property<String> getPoolName();

    @Internal
    abstract public RegularFileProperty getKnownHostsFile();

    @Internal
    abstract public RegularFileProperty getKubeConfigFile();

    @Internal
    abstract public Property<String> getDistributionName();

    @Inject
    abstract public ExecOperations getExecOperations();

    public void initialize(TestbedExtension extension, TaskProvider<Start> startDomain, TaskProvider<CopyKubeConfig> readKubeConfig) {
        getDomainName().convention(extension.getDomain().getName());
        getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
        getKnownHostsFile().convention(startDomain.get().getKnownHostsFile());
        getPoolName().convention(extension.getPool().getName());
        getDistributionName().convention(extension.getBaseImage().getName());
    }

    @TaskAction
    public void printStatus() throws Throwable {
        getLogger().quiet("Status of {}", getDomainName().get());

        try (var testbed = new Testbed(getDomainName().get())) {

            check("Testbed", testbed::withDomain, (CheckedConsumer<Domain>) domain -> {

                info("IP Address", domain::getTestbedHostAddress);

                SecureShell shell = testbed.getExec(getExecOperations(), getKnownHostsFile());
                check("ssh uname", shell.execute("uname", "-norm"), result -> {
                    info("stdout", result::getStdout);
                    info("exit code", result::getExitValue);
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

                var pool = domain.getPool(getPoolName().get());
                check("Pool", pool::run, p -> {
                    info("Pool Name", p::getName);
                    info("Pool Autostart", p::getAutostart);
                    info("Pool isActive", p::isActive);
                    evaluate2("Pool Volumes", p::listVolumes,
                            status -> status.check(vols -> Arrays.asList(vols).contains("root.qcow2") && Arrays.asList(vols).contains("cidata.img"))
                                    .ok(vols -> String.join(", ", vols))
                                    .error("Nicht genau drei Volumes")
                    );
                });

                check("Registry", domain::withRegistry, registry -> {
                    info("Address", registry::getAddress);
                    info("Upload Image", () -> registry.uploadImage("hello-world:latest"));
                    evaluate2("Catalogs", registry::listCatalogs,
                            status -> status.check(catalogs -> catalogs.contains("hello-world"))
                                    .ok(catalogs -> String.join(", ", catalogs))
                                    .error("missing catalog hello-world")
                    );
                });

                var distribution = new Distribution(getProject().getObjects(), getDistributionName());
                info("XDG_DATA_HOME", () -> distribution.getDownloadDir());
                info("Distribution", () -> distribution.getName().get());
            });
        }
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

    private <T> void check(String label, CheckedConsumer<CheckedConsumer<T>> fn, CheckedConsumer<T> with) {
        try {
            fn.accept(with);
            getLogger().quiet(String.format("✓ %-15s: %s", label, "OK"));
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }


}
