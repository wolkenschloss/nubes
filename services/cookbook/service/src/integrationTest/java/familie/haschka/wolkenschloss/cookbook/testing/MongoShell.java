package familie.haschka.wolkenschloss.cookbook.testing;

import java.io.IOException;
import java.util.Objects;

/**
 * This is a wrapper for the execution of scripts in a MongoDB shell, which is in a test container.
 */
public class MongoShell {

    private final MongoShellContainer container;

    public MongoShell(MongoShellContainer container) {
        this.container = Objects.requireNonNull(container);
    }

    /**
     * The method executes a Javascript in the MongoDb Shell.
     *
     * @param script The Javascript to be executed.
     * @return The method returns a {@link FluentExecResult} with the output of the command and the status code.
     */
    public FluentExecResult eval(String script) throws IOException, InterruptedException {
        return new FluentExecResult(container.execInContainer(
                "/bin/bash",
                "-c",
                String.format("mongosh --quiet --eval \"%s\" $CONNECTION_STRING", script)));
    }

    /**
     * Lists the contents of a directory in the container.
     *
     * The method is mainly used for debugging purposes.
     *
     * @param path The path of a directory in the container
     * @return The method returns a {@link FluentExecResult} with the output of the command and the status code.
     */
    @SuppressWarnings("unused")
    public FluentExecResult ls(String path) throws IOException, InterruptedException {
        return new FluentExecResult(container.execInContainer(
                "ls",
                "-lhaR",
                path
        ));
    }

    /**
     * Lists the container's environment variables and their values.
     * @return The method returns a {@link FluentExecResult} with the output of the command and the status code.
     */
    @SuppressWarnings("unused")
    public FluentExecResult printenv() throws IOException, InterruptedException {
        return new FluentExecResult(container.execInContainer("printenv"));
    }
}
