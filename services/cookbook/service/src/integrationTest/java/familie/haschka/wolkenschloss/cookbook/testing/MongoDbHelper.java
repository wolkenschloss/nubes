package familie.haschka.wolkenschloss.cookbook.testing;

import com.mongodb.client.MongoClients;
import io.quarkus.test.common.DevServicesContext;
import io.smallrye.mutiny.Multi;
import org.jboss.logging.Logger;

public class MongoDbHelper implements DevServicesContext.ContextAware {

    private static Logger logger = Logger.getLogger(MongoDbHelper.class);
    private String connectionString;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.connectionString = context.devServicesProperties().get("quarkus.mongodb.connection-string");

        context.devServicesProperties().forEach((k, v) -> {
            logger.infov("[{0}]: {1}", k, v);
        });
    }

    public void drop() {
        logger.infov("drop database {0}", connectionString);

        try (var client = MongoClients.create(connectionString)) {
            client.getDatabase("cookbook").listCollectionNames().forEach(s ->
                    logger.infov("collection {0}", s));

            client.getDatabase("cookbook").getCollection("Ingredient").drop();
        }
    }
}
