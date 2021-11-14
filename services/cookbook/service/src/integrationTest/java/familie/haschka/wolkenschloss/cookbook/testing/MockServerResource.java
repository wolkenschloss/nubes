package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.shaded.org.apache.commons.lang.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

public class MockServerResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

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
        return new MockServerContainer();
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

    record ContainerNetwork(String id) implements Network {

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public void close() {
        }

        @NotNull
        @Override
        public Statement apply(@NotNull Statement base, @NotNull Description description) {
            throw new NotImplementedException();
        }
    }
}
