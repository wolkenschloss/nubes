package family.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.mockserver.Version;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.AbstractMap;
import java.util.Map;

public class MockServerResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final Logger logger = Logger.getLogger(MockServerResource.class);

    private static final DockerImageName IMAGE_NAME = DockerImageName.parse("mockserver/mockserver");
    private static final String IMAGE_TAG = String.format("mockserver-%s", Version.getVersion());

    public static final String SERVER_HOST_CONFIG = "mockserver.server.host";
    public static final String SERVER_PORT_CONFIG = "mockserver.server.port";
    public static final String CLIENT_HOST_CONFIG = "mockserver.client.host";
    public static final String CLIENT_PORT_CONFIG = "mockserver.client.port";

    private static final String ALIAS = "mockserver";

    private MockServerContainer mockServerContainer = null;

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.mockServerContainer = context.containerNetworkId()
                .map(ContainerNetwork::new)
                .map(this::container)
                .orElseGet(this::container);
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

        return Map.ofEntries(
                new AbstractMap.SimpleEntry<>(CLIENT_HOST_CONFIG, "localhost"),
                new AbstractMap.SimpleEntry<>(CLIENT_PORT_CONFIG, mockServerContainer.getServerPort().toString()),
                new AbstractMap.SimpleEntry<>(SERVER_HOST_CONFIG, host()),
                new AbstractMap.SimpleEntry<>(SERVER_PORT_CONFIG, port().toString())
        );
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
