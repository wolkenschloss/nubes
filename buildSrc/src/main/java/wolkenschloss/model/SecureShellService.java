package wolkenschloss.model;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.process.ExecOperations;
import wolkenschloss.domain.DomainOperations;
import wolkenschloss.CheckedConsumer;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Vector;

public abstract class SecureShellService implements BuildService<SecureShellService.Params> {

    public interface Params extends org.gradle.api.services.BuildServiceParameters {
        Property<DomainOperations> getDomainOperations();
        RegularFileProperty getKnownHostsFile();
    }

    private final String ip;

    public SecureShellService() throws Throwable {
        DomainOperations domainOperations = getParameters().getDomainOperations().get();
        this.ip = domainOperations.getTestbedHostAddress();
    }

    public static class Result {

        private final String stdout;
        private final String stderr;
        private final int exitValue;

        @SuppressWarnings("CdiInjectionPointsInspection")
        public Result(String stdout, String stderr, int exitValue) {

            this.stdout = stdout;
            this.stderr = stderr;
            this.exitValue = exitValue;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public int getExitValue() {
            return exitValue;
        }
    }

    public CheckedConsumer<CheckedConsumer<Result>> execute(ExecOperations execOperations, Object... args) {

        return (CheckedConsumer<Result> consumer) -> {

            var knownHostsFile = getParameters().getKnownHostsFile().getAsFile().get().getPath();
            var arguments = new Vector<>();
            arguments.addAll(Arrays.asList("-o",
                    String.format("UserKnownHostsFile=%s", knownHostsFile),
                    ip));
            arguments.addAll(Arrays.asList(args));

            try (var stdout = new ByteArrayOutputStream()) {
                try (var stderr = new ByteArrayOutputStream()) {
                    var result = execOperations.exec(spec -> {
                        spec.commandLine("ssh")
                                .args(arguments)
                                .setStandardOutput(stdout)
                                .setErrorOutput(stderr);
                    }).assertNormalExitValue();

                    consumer.accept(new Result(
                            stdout.toString().trim(),
                            stderr.toString().trim(),
                            result.getExitValue()));
                }
            }
        };
    }
}
