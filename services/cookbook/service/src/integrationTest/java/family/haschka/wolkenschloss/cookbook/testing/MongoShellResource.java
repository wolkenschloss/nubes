package family.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;

import javax.ws.rs.core.UriBuilder;
import java.util.Map;
import java.util.Objects;

public class MongoShellResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    private static final Logger logger = Logger.getLogger(MongoShellResource.class.getName());
    private MongoShellContainer container;

    @Override
    public Map<String, String> start() {
        Objects.requireNonNull(this.container)
                .start();

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
                .orElseGet(MongoShellContainer::create)
                .withEnv("CONNECTION_STRING", sanitisedConnectionString(context.devServicesProperties()));
    }

    private String sanitisedConnectionString(Map<String, String> properties) {
        return UriBuilder.fromUri(properties.get("quarkus.mongodb.connection-string"))
                .replaceQueryParam("uuidRepresentation")
                .build()
                .toString();
    }

    @Override
    public void inject(TestInjector testInjector) {
        testInjector.injectIntoFields(
                new MongoShell(this.container),
                field -> field.getType().isAssignableFrom(MongoShell.class));
    }

}
