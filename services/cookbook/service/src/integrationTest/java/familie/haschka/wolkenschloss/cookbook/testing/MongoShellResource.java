package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerLaunchException;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class MongoShellResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private static final Logger logger = Logger.getLogger(MongoShellResource.class);
    private MongoShellContainer container;
    private String connectionString;

    @Override
    public Map<String, String> start() {
        try {
            this.container.start();
        } catch (ContainerLaunchException exception) {
            logger.errorv(exception, "Cannot start container");
        }

        return null;
    }

    @Override
    public void stop() {
        if (this.container != null) {
            this.container.stop();
        }
    }


    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.container = context.containerNetworkId()
                .map(ContainerNetwork::new)
                .map(MongoShellContainer::create)
                .orElseGet(MongoShellContainer::create);

        this.connectionString = Optional.ofNullable(context.devServicesProperties()
                .get("quarkus.mongodb.connection-string"))
                .orElseThrow();

        logger.infov("using connection string: {0}", connectionString);
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(
                new MongoShell(),
                field -> field.getType().isAssignableFrom(MongoShell.class));
    }

    public class MongoShell {

        public Container.ExecResult eval(String script) throws IOException, InterruptedException {
            logger.infov("mongosh --eval {0} {1}", script, sanitisedConnectionString());

            return container.execInContainer(
                    "mongosh",
                    "--quiet",
                    "--eval",
                    script,
                    sanitisedConnectionString());
        }

        public Container.ExecResult ls() throws IOException, InterruptedException {
            return container.execInContainer(
                    "ls",
                    "-lhaR",
                    "/opt/"
            );
        }
        private String sanitisedConnectionString() {
            return UriBuilder.fromUri(connectionString)
                    .replaceQueryParam("uuidRepresentation")
                    .build()
                    .toString();
        }

        public static void log(Container.ExecResult result) {
            logger.infov("output: {0}", result.getStdout());

            if(result.getExitCode() != 0) {
                logger.errorv("stderr: {0}", result.getStderr());
            }
        }
    }
}
