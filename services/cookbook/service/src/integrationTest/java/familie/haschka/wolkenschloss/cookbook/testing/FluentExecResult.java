package familie.haschka.wolkenschloss.cookbook.testing;

import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.Container;

public record FluentExecResult(Container.ExecResult result) {
    private static final Logger log = LoggerFactory.getLogger(MongoShell.class);

    FluentExecResult log() {

        log.info(result.getStdout());

        if (result.getExitCode() != 0) {
            log.error("exit code {}: {}", result().getExitCode(), result().getStderr());
        }

        return this;
    }

    public FluentExecResult verify() {
        Assumptions.assumeTrue(result().getExitCode() == 0);
        return this;
    }
}