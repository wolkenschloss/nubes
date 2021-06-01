package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.testing.MongoDbResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@QuarkusTest
@QuarkusTestResource(MongoDbResource.class)
@TestHTTPEndpoint(RecipeRessource.class)
public class FirstIntegrationTest {

    private static final Logger logger = Logger.getLogger(FirstIntegrationTest.class);

    @ConfigProperty(name = "quarkus.http.port")
    String quarkusPort;

    @Test
    public void checkDefaultHttpPort() {
        logger.infof("quarkus.http.port = %s", quarkusPort);
        Assertions.assertEquals("0", quarkusPort);
    }

    @Test
    public void checkMongoDbHostConfiguration() {
        var host = System.getProperty("quarkus.mongodb.hosts");
        logger.warnf("quarkus.mongodb.hosts = %s", host);
        Assertions.assertNotNull(host);
    }
}
