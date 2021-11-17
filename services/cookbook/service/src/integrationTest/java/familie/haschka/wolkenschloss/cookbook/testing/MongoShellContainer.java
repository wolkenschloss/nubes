package familie.haschka.wolkenschloss.cookbook.testing;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class MongoShellContainer extends GenericContainer<MongoShellContainer> {

    public MongoShellContainer() {
        super(                new ImageFromDockerfile("nubes/mongosh:latest", false)
                        .withFileFromClasspath("Dockerfile", "mongosh/docker/Dockerfile")
                        .withFileFromClasspath("listCollections.js", "mongosh/scripts/listCollections.js")
                        .withFileFromClasspath("dropCollections.js", "mongosh/scripts/dropCollections.js")
        );
    }
}
