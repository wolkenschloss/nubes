package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Läuft nur mit -Dquarkus.container-image.build=true
public class MockServerResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private static final String ALIAS = "mockserver";

    public static final String APPLICATION_TEMPLATE_CONFIG = "mockserver.application.template";
    public static final String TESTCLIENT_HOST_CONFIG = "mockserver.testclient.host";
    public static final String TESTCLIENT_PORT_CONFIG = "mockserver.testclient.port";

    private static final Logger logger = Logger.getLogger(MockServerResource.class);

    private Optional<String> containerNetworkId = Optional.empty();

    private Optional<MockServerContainer> mockServerContainer = Optional.empty();

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.containerNetworkId = context.containerNetworkId();
        this.containerNetworkId.ifPresent(id -> logger.infov("container network id: {0}", id));
    }

    @Override
    public Map<String, String> start() {
        this.mockServerContainer = containerNetworkId.map(
                id -> new MockServerContainer()
                        .withNetwork(new DockerNetwork(id))
                        .withNetworkAliases(ALIAS));

        this.mockServerContainer.ifPresent(GenericContainer::start);

        logger.infov("mock server started.");
        var config = new HashMap<String, String>();
        config.put(APPLICATION_TEMPLATE_CONFIG, String.format("http://%s:%d", ALIAS, 1080));
        config.put(TESTCLIENT_HOST_CONFIG, "localhost");
        config.put(TESTCLIENT_PORT_CONFIG, mockServerContainer.orElseThrow().getFirstMappedPort().toString());

        config.put("mockserver.host", ALIAS);
        config.put("mockserver.port",
                mockServerContainer.orElseThrow().getExposedPorts().stream().findFirst().map(port -> port.toString()).orElseThrow());
        config.put("mockserver.mappedport", mockServerContainer.orElseThrow().getFirstMappedPort().toString());
        return config;
    }

    @Override
    public void stop() {
        logger.info("stop mock server");
        mockServerContainer.ifPresent(GenericContainer::stop);
        mockServerContainer = Optional.empty();
    }

    static class DockerNetwork implements Network {

        private final String id;

        public DockerNetwork(String id) {

            this.id = id;
        }

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
            return null;
        }
    }
}
