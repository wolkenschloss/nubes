package familie.haschka.wolkenschloss.cookbook.testing;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

public class MongoShellContainer extends GenericContainer<MongoShellContainer> {

    public static final String RESOURCE_PATH = "mongosh/";
    public static final DockerImageName IMAGE_NAME = DockerImageName.parse("nubes/mongosh");
    private static final String DEFAULT_TAG = "1.0";

    public MongoShellContainer() {
        super(new ImageFromDockerfile(IMAGE_NAME.withTag(DEFAULT_TAG).toString(), false)
                        .withFileFromClasspath(".", RESOURCE_PATH)
        );
    }

    public static MongoShellContainer create(ContainerNetwork network) {
        return new MongoShellContainer()
                .withNetwork(network)
                .withReuse(true);
    }

    public static MongoShellContainer create(String networkMode) {
        return new MongoShellContainer()
                .withNetworkMode(networkMode)
                .withReuse(true);
    }
}
