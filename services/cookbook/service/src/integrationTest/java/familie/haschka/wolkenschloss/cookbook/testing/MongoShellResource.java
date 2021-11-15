package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.testcontainers.containers.ContainerLaunchException;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Map;

public class MongoShellResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private static final Logger logger = Logger.getLogger(MongoShellResource.class);
    private static final DockerImageName MONGO_CLIENT = DockerImageName.parse("alpine:3.14");
    private GenericContainer container;
    private String networkId;
    private String connectionString;

    @Override
    public Map<String, String> start() {
        this.container = new GenericContainer(
                new ImageFromDockerfile("nubes/mongosh:latest", false)
                        .withFileFromClasspath("Dockerfile", "mongosh/docker/Dockerfile")
                        .withFileFromClasspath("listCollections.js", "mongosh/scripts/listCollections.js")
                        .withFileFromClasspath("dropCollections.js", "mongosh/scripts/dropCollections.js")
        )
                .withNetwork(new ContainerNetwork(networkId));
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
        this.connectionString = context.devServicesProperties()
                .getOrDefault("quarkus.mongodb.connection-string", null);

        this.networkId = context.containerNetworkId().orElse(null);

        logger.infov("quarkus.mongodb.connection-string: {0}", connectionString);
        logger.infov("docker network id: {0}", networkId);
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(new MongoShell(container),
                new TestInjector.AnnotatedAndMatchesType(InjectMongoShell.class, MongoShell.class));
    }

    public class MongoShell {
        private final GenericContainer container;

        MongoShell(GenericContainer container) {

            this.container = container;
        }

        public void exec() throws IOException, InterruptedException {
            if (this.container != null && this.container.isRunning()) {
                var connection = UriBuilder.fromUri(connectionString)
                        .replaceQueryParam("uuidRepresentation").build();

                var script = "/opt/mongosh/listCollections.js";
                logger.infov("executing mongosh {0} {1}", connection.toString(), script);
                var result = this.container.execInContainer(
                        "mongosh",
                        "--quiet",
                        connection.toString(),
                        script
                        //String.format("mongosh %s listCollections.js", connectionString)
                );
                var output = result.getStdout();
                logger.infov("output: {0}", output);

                if(result.getExitCode() != 0) {
                    var error = result.getStderr();
                    logger.errorv("stderr: {0}", error);
                }
            }
        }

        public void drop() throws IOException, InterruptedException {
            if (this.container != null && this.container.isRunning()) {
                var connection = UriBuilder.fromUri(connectionString)
                        .replaceQueryParam("uuidRepresentation").build();

                var script = "/opt/mongosh/dropCollections.js";
                logger.infov("executing mongosh {0} {1}", connection.toString(), script);
                var result = this.container.execInContainer(
                        "mongosh",
                        connection.toString(),
                        script
                        //String.format("mongosh %s listCollections.js", connectionString)
                );
                var output = result.getStdout();
                logger.infov("output: {0}", output);

                if(result.getExitCode() != 0) {
                    var error = result.getStderr();
                    logger.errorv("stderr: {0}", error);
                }
            }
        }
    }
}
