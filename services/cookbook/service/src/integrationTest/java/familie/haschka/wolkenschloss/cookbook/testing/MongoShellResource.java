package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.ContainerLaunchException;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;

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

    private MongoShellContainer container(ContainerNetwork network) {
        return container().withNetwork(network);
    }

    @NotNull
    private MongoShellContainer container() {
        return new MongoShellContainer();
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
                .map(this::container)
                .orElseGet(this::container);

        this.connectionString = context.devServicesProperties()
                .getOrDefault("quarkus.mongodb.connection-string", null);

        logger.infov("quarkus.mongodb.connection-string: {0}", connectionString);
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(new MongoShell(),
                new TestInjector.AnnotatedAndMatchesType(InjectMongoShell.class, MongoShell.class));
    }

    public class MongoShell {

        public void exec() throws IOException, InterruptedException {
            if (container != null && container.isRunning()) {
                var connection = sanitisedConnectionString();

                var script = "/opt/mongosh/listCollections.js";
                logger.infov("executing mongosh {0} {1}", connection, script);
                var result = container.execInContainer(
                        "mongosh",
                        "--quiet",
                        connection,
                        script
                );

                log(result);
            }
        }

        private String sanitisedConnectionString() {
            return UriBuilder.fromUri(connectionString)
                    .replaceQueryParam("uuidRepresentation")
                    .build()
                    .toString();
        }

        private void log(Container.ExecResult result) {
            logger.infov("output: {0}", result.getStdout());

            if(result.getExitCode() != 0) {
                logger.errorv("stderr: {0}", result.getStderr());
            }
        }

        public void drop() throws IOException, InterruptedException {
            if (container != null && container.isRunning()) {
                var connection = sanitisedConnectionString();

                var script = "/opt/mongosh/dropCollections.js";
                logger.infov("executing mongosh {0} {1}", connection, script);
                var result = container.execInContainer(
                        "mongosh",
                        connection,
                        script
                );

                log(result);
            }
        }
    }
}
