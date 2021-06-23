package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.libvirt.DomainInfo;

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
    abstract public RegularFileProperty getServerKeyFile();


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

    private <T> void evaluate2(String label, CheckedFunction<T> fn, Function<Check<T>, StatusChecker<T>> r) {
        try {
            r.apply(new StatusBuilder<T>(fn)).run(label);
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }

    @FunctionalInterface
    public interface CheckedFunction<T> {
        T apply() throws Throwable;
    }

    private <T> void info(String label, CheckedFunction<T> fn) {
        try {
            getLogger().quiet(String.format("✓ %-15s: %s", label, fn.apply()));
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }

    private <T> void checklog(String label, CheckedFunction<T> fn, Consumer<T> with) {
        try {
            T result = fn.apply();
            getLogger().quiet(String.format("✓ %-15s: %s", label, result));
            with.accept(result);
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }

    private <T> void checkok(String label, CheckedFunction<T> fn, Consumer<T> with) {
        try {
            T result = fn.apply();
            getLogger().quiet(String.format("✓ %-15s: %s", label, "OK"));
            with.accept(result);
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }

    @FunctionalInterface
    public interface CheckedMethod {
        void apply() throws Throwable;
    }

    private void checkok(String label, CheckedMethod fn) {
        try {
            fn.apply();
            getLogger().quiet(String.format("✓ %-15s: %s", label, "OK"));
        } catch (Throwable e) {
            getLogger().error(String.format("✗ %-15s: %s", label, e.getMessage()));
        }
    }

    interface Check<T> {
        OkMessage<T> check(Predicate<T> p);
    }

    interface OkMessage<T> {
        ErrorMessage<T> ok(Function<T, String> message);
    }

    interface ErrorMessage<T> {
        StatusChecker<T> error(String message);
    }

    interface StatusChecker<T> {
        void run(String label);
    }

    class StatusBuilder<T> implements Check<T>, OkMessage<T>, ErrorMessage<T>, StatusChecker<T> {

        private final CheckedFunction<T> fn;
        private Predicate<T> predicate;
        private String errorMessage;
        private Function<T, String> okMessageProducer;

        public StatusBuilder(CheckedFunction<T> fn) {
            this.fn = fn;
        }

        public OkMessage<T> check(Predicate<T> p) {
            this.predicate = p;
            return this;
        }

        public ErrorMessage<T> ok(Function<T, String> message) {
            this.okMessageProducer = message;
            return this;
        }

        public ErrorMessage<T> ok(String message) {
            this.okMessageProducer = (Void) -> message;
            return this;
        }

        public StatusChecker<T> error(String message) {
            this.errorMessage = message;
            return this;
        }

        public void run(String label) {
            T value = null;
            try {
                value = fn.apply();
            } catch (Throwable throwable) {
                getLogger().error(String.format("✗ %-15s: %s", label, throwable.getMessage()));
                return;
            }

            if (predicate.test(value)) {
                getLogger().quiet(String.format("✓ %-15s: %s", label, okMessageProducer.apply(value)));
            } else {
                getLogger().error(String.format("✗ %-15s: %s", label, errorMessage));
            }
        }
    }

    @TaskAction
    public void printStatus() {
        getLogger().quiet("Status of {}", getDomainName().get());

        var domain = new Domain(getDomainName().get(), 0);

        checklog("IP Address", domain::getTestbedHostAddress, ip -> {
            try (var stdout = new ByteArrayOutputStream()) {
                var result = getExecOperations().exec(e -> {
                    e.commandLine("ssh");
                    e.args("-o", String.format("UserKnownHostsFile=%s", getKnownHostsFile().get().getAsFile().getAbsolutePath()),
                            ip, "uname", "-nrom");
                    e.setStandardOutput(stdout);
                });
                evaluate("SSH Connection", result.getExitValue(), exitCode -> exitCode == 0);
                evaluate("uname", stdout.toString().trim(), uname -> uname != null && !uname.isEmpty());
            } catch (IOException e) {
                getLogger().error("Something went wrong", e);
            }
        });

        checkok("Connect", domain::getInfo, info -> {
            evaluate("State", info.state, s -> s == DomainInfo.DomainState.VIR_DOMAIN_RUNNING);
            evaluate("Memory (MB)", info.memory / 1024, m -> m >= 4096);
            evaluate("Virtual CPU's", info.nrVirtCpu, n -> n > 1);
        });

        evaluate2("Server Key", () -> getServerKeyFile().getAsFile().get().toPath(),
                status -> status.check(Files::exists)
                        .ok(Path::toString)
                        .error("missing")
        );

        evaluate2("Kubeconfig", () -> getKubeConfigFile().getAsFile().get().toPath(),
                status -> status.check(Files::exists)
                        .ok(Path::toString)
                        .error("missing"));

        checkok("Pool", () -> domain.withPool(getPoolName().get(), pool -> {
            info("Pool Name", pool::getName);
            info("Pool Autostart", pool::getAutostart);
            info("Pool isActive", pool::isActive);
            evaluate2("Pool Volumes", pool::listVolumes,
                    status -> status.check(vols -> Arrays.stream(vols).count() == 3)
                            .ok(vols -> String.join(", ", vols))
                            .error("Nicht genau drei Volumes")
            );
        }));
    }
}
