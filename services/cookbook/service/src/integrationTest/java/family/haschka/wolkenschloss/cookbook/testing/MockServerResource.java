package family.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;

public class MockServerResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final DockerImageName IMAGE_NAME = DockerImageName.parse("jamesdbloom/mockserver");
    private static final String IMAGE_TAG = "mockserver-5.5.4";

    public static final String SERVER_HOST_CONFIG = "mockserver.server.host";
    public static final String SERVER_PORT_CONFIG = "mockserver.server.port";
    public static final String CLIENT_HOST_CONFIG = "mockserver.client.host";
    public static final String CLIENT_PORT_CONFIG = "mockserver.client.port";

    private static final String ALIAS = "mockserver";
    private static final Logger logger = Logger.getLogger(MockServerResource.class);

    private MockServerContainer mockServerContainer = null;
    private final HashMap<String, String> config = new HashMap<>();

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.mockServerContainer = context.containerNetworkId()
                .map(ContainerNetwork::new)
                .map(this::container)
                .orElseGet(this::container);

        context.containerNetworkId().ifPresent(id -> logger.infov("container network id: {0}", id));
    }

    private MockServerContainer container(ContainerNetwork network) {
        logger.infov("container network id: {0}", network.id());
        return container()
                .withNetwork(network)
                .withNetworkAliases(ALIAS);
    }

    @NotNull
    private MockServerContainer container() {
        return new MockServerContainer(IMAGE_NAME.withTag(IMAGE_TAG));
    }

    @Override
    public Map<String, String> start() {

        logger.infov("starting mockserver");
        this.mockServerContainer.start();

        config.put(CLIENT_HOST_CONFIG, "localhost");
        config.put(CLIENT_PORT_CONFIG, mockServerContainer.getServerPort().toString());
        config.put(SERVER_HOST_CONFIG, host());
        config.put(SERVER_PORT_CONFIG, port().toString());

        return config;
    }

    private String host() {
        if (usesContainerNetwork()) {
            return ALIAS;
        } else {
            return "localhost";
        }
    }

    private boolean usesContainerNetwork() {
        return mockServerContainer.getNetworkAliases().contains(ALIAS);
    }

    private Integer port() {
        if (usesContainerNetwork()) {
            return MockServerContainer.PORT;
        } else {
            return mockServerContainer.getServerPort();
        }
    }

    @Override
    public void stop() {
        logger.info("stop mockserver");

        if (mockServerContainer != null) {
            mockServerContainer.stop();
            mockServerContainer = null;
        }
    }

}
