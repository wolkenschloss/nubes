package wolkenschloss.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.libvirt.DomainInfo;
import wolkenschloss.Domain;
import wolkenschloss.SecureShell;
import wolkenschloss.Testbed;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
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

    @Inject
    abstract public ExecOperations getExecOperations();

    private <T> void evaluate(String label, T value, Predicate<T> requiredCondition) {

        if (requiredCondition.test(value)) {
            getLogger().quiet(String.format("✓ %-15s: %s", label, value));
        } else {
            getLogger().error(String.format("✗ %-15s: %s", label, value));
        }
    }

    private <T> void evaluate2(String label, CheckedSupplier<T> fn, Function<Check<T>, StatusChecker<T>> check) {
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

    private <T> void checkResult(String label, CheckedSupplier<T> fn, Consumer<T> with) {
        try {
            T result = fn.apply();
            getLogger().quiet(String.format("✓ %-15s: %s", label, result));
            with.accept(result);
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

    @TaskAction
    public void printStatus() throws Throwable {
        getLogger().quiet("Status of {}", getDomainName().get());

        try (var testbed = new Testbed(getDomainName().get())) {

            check("Testbed", testbed::withDomain, (CheckedConsumer<Domain>) domain -> {

                SecureShell shell = testbed.getExec(getExecOperations(), getKnownHostsFile());
                check("SSH", shell.execute("uname"), result -> {
                    info("stdout", result::getStdout);
                    info("exit code", result::getExitValue);
                });

                checkResult("IP Address", domain::getTestbedHostAddress, ip -> {
                    try (var stdout = new ByteArrayOutputStream()) {
                        var result = getExecOperations().exec(e -> {
                            e.commandLine("ssh");
                            e.args("-o", String.format("UserKnownHostsFile=%s", getKnownHostsFile().get().getAsFile().getAbsolutePath()),
                                    ip, "uname", "-nrom");
                            e.setStandardOutput(stdout);
                        });
                        evaluate("SSH Connection", result.getExitValue(), exitCode -> exitCode == 0);
                        info("uname", () -> stdout.toString().trim());
                    } catch (IOException e) {
                        getLogger().error("Something went wrong", e);
                    }
                });

                check("Connect", domain::withInfo, info -> {
                    evaluate("State", info.state, s -> s == DomainInfo.DomainState.VIR_DOMAIN_RUNNING);
                    evaluate("Memory (MB)", info.memory / 1024, m -> m >= 4096);
                    evaluate("Virtual CPU's", info.nrVirtCpu, n -> n > 1);
                });

                evaluate2("K8s configuration", () -> getKubeConfigFile().getAsFile().get().toPath(),
                        status -> status.check(Files::exists)
                                .ok(Path::toString)
                                .error("missing"));

                var pool = domain.getPool(getPoolName().get());
                check("Pool", pool::run, p -> {
                    info("Pool Name", p::getName);
                    info("Pool Autostart", p::getAutostart);
                    info("Pool isActive", p::isActive);
                    evaluate2("Pool Volumes", p::listVolumes,
                            status -> status.check(vols -> Arrays.stream(vols).count() == 3)
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
            });
        }
    }
}
