package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.process.ExecOperations;
import wolkenschloss.task.CheckedConsumer;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Vector;

public class SecureShell {
    private final Testbed testbed;
    private final ExecOperations operations;
    private final RegularFileProperty knownHostFile;

    public SecureShell(
            Testbed testbed,
            @SuppressWarnings("CdiInjectionPointsInspection") ExecOperations operations,
            @SuppressWarnings("CdiInjectionPointsInspection") RegularFileProperty knownHostFile) {
        this.testbed = testbed;
        this.operations = operations;
        this.knownHostFile = knownHostFile;
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

    public CheckedConsumer<CheckedConsumer<Result>> execute(Object... args) throws Throwable {
//        e.args("-o", String.format("UserKnownHostsFile=%s", getKnownHostsFile().get().getAsFile().getAbsolutePath()),
//                ip, "uname", "-nrom");

        return (CheckedConsumer<Result> consumer) -> {
            testbed.withDomain(domain -> {
                var ip = domain.getTestbedHostAddress();

                var arguments = new Vector<>();
                arguments.addAll(Arrays.asList("-o",
                        String.format("UserKnownHostsFile=%s", this.knownHostFile.get().getAsFile().getPath()),
                        ip));
                arguments.addAll(Arrays.asList(args));

                try (var stdout = new ByteArrayOutputStream()) {
                    try (var stderr = new ByteArrayOutputStream()) {
                        var result = operations.exec(spec -> {
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
            });
        };
    }
}
