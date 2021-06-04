package familie.haschka.wolkenschloss.cookbook;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;

public class MongoDbResource implements QuarkusTestResourceLifecycleManager {

    private final MongoDBContainer container;

    public MongoDbResource() {
        var image = DockerImageName.parse("mongo:latest");
        this.container = new MongoDBContainer(image)
                .withExposedPorts(27017)
                .waitingFor(Wait.forLogMessage(".*Waiting for connections.*", 1));
    }

    @Override
    public Map<String, String> start() {
        container.start();
        var host = container.getHost();
        var port = container.getFirstMappedPort();
        return Collections.singletonMap("quarkus.mongodb.connection-string", "mongodb://" + host + ":" + port.toString() + "/?uuidRepresentation=STANDARD");
    }

    @Override
    public void stop() {
        container.stop();
    }
}
