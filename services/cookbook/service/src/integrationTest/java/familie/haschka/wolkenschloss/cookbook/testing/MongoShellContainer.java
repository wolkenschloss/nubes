package familie.haschka.wolkenschloss.cookbook.testing;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;


/**
 * MongoShellContainer is a test container that contains a MongoShell (mongosh).
 *
 *
 * The container is designed to perform database operations in a MongoDB test container started by Quarkus DevService.
 * The image of the container is created using the Dockerfile in the resource directory {@code mongosh}
 *
 *
 * Quarkus DevService starts the MongoDB test container in its own network if the build option
 * {@code -Dquarkus.container-image.build=true} is used. So that MongoShell can establish a connection
 * to the database, the {@link MongoShellContainer} must use the same Docker network as Quarkus DevService.
 * In this case, the container must be created with {@link #create(ContainerNetwork)}.
 *
 *
 * @see <a href="https://quarkus.io/guides/dev-services">Dev Services Overview</a>
 */
public class MongoShellContainer extends GenericContainer<MongoShellContainer> {

    public static final String RESOURCE_PATH = "mongosh/";
    public static final DockerImageName IMAGE_NAME = DockerImageName.parse("nubes/mongosh");
    private static final String DEFAULT_TAG = "1.0";

    public MongoShellContainer() {
        super(new ImageFromDockerfile(IMAGE_NAME.withTag(DEFAULT_TAG).toString(), false)
                        .withFileFromClasspath(".", RESOURCE_PATH)
        );
    }

    /**
     * Creates a container with a Mongo Shell connected to a specific docker network.
     *
     * @param network Docker Network created by Quarkus DevService. Quarkus creates such a network if the build
     *                option {@code -Dquarkus.container-image.build=true} is set.
     *
     * @return A new instance of a {@link MongoShellContainer}
     */
    public static MongoShellContainer create(ContainerNetwork network) {
        return new MongoShellContainer()
                .withNetwork(network)
                .withReuse(true);
    }

    /**
     * Creates a container with a Mongo Shell connected to the docker host network.
     *
     * The container is started with the network mode host so that the connection
     * to the MongoDb test container started by Quarkus Dev Services works.
     *
     * @return A new instance of a MongoShellContainer
     *
     * @see <a href="https://docs.docker.com/network/host/">Docker docs: Use host networking</a>
     */
    public static MongoShellContainer create() {
        return new MongoShellContainer()
                .withNetworkMode("host")
                .withReuse(true);
    }
}
