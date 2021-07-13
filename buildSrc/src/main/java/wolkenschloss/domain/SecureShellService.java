package wolkenschloss.domain;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.process.ExecOperations;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;
import java.util.function.Consumer;

public class SecureShellService {

    private final ExecOperations execOperations;
    private final String ip;
    private final RegularFileProperty knownHostsFile;

    public SecureShellService(ExecOperations execOperations, String ip, RegularFileProperty knownHostsFile) {
        this.execOperations = execOperations;
        this.ip = ip;
        this.knownHostsFile = knownHostsFile;
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

    @FunctionalInterface
    public interface ShellCommand<T> {
        void execute(T fn);
    }

    public ShellCommand<Consumer<Result>> command(Object... args) {
        return (Consumer<Result> consumer) -> {

            var arguments = new Vector<>();
            arguments.addAll(Arrays.asList("-o",
                    String.format("UserKnownHostsFile=%s", knownHostsFile.get().getAsFile().getPath()),
                    ip));
            arguments.addAll(Arrays.asList(args));

            try (var stdout = new ByteArrayOutputStream()) {
                try (var stderr = new ByteArrayOutputStream()) {
                    var result = execOperations.exec(spec -> spec.commandLine("ssh")
                            .args(arguments)
                            .setStandardOutput(stdout)
                            .setErrorOutput(stderr)).assertNormalExitValue();

                    consumer.accept(new Result(
                            stdout.toString().trim(),
                            stderr.toString().trim(),
                            result.getExitValue()));
                }
            } catch (IOException exception) {
                throw new RuntimeException("Can not create streams.", exception);
            }
        };
    }

    public Consumer<Consumer<Result>> withCommand(Object... args) {
        return (Consumer<Result> consumer) -> command(args).execute(consumer);
    }
}
