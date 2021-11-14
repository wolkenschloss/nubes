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

import javax.ws.rs.core.UriBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MockServerResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    public static final String APPLICATION_TEMPLATE_CONFIG = "mockserver.application.template";
    public static final String TESTCLIENT_HOST_CONFIG = "mockserver.testclient.host";
    public static final String TESTCLIENT_PORT_CONFIG = "mockserver.testclient.port";
    private static final String ALIAS = "mockserver";
    private static final Logger logger = Logger.getLogger(MockServerResource.class);


    private MockServerContainer mockServerContainer = null;
    private HashMap<String, String> config = new HashMap<>();

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {
        this.mockServerContainer = context.containerNetworkId()
                .map(ContainerNetwork::new)
                .map(network -> create()
                        .withNetwork(network)
                        .withNetworkAliases(ALIAS))
                .orElseGet(this::create);


        context.containerNetworkId().ifPresent(id -> logger.infov("container network id: {0}", id));
    }

    @NotNull
    private MockServerContainer create() {
        return new MockServerContainer();
    }

    @Override
    public Map<String, String> start() {

        logger.infov("starting mockserver");
        this.mockServerContainer.start();

        config.put(TESTCLIENT_HOST_CONFIG, "localhost");
        config.put(TESTCLIENT_PORT_CONFIG, mockServerContainer.getServerPort().toString());

        UriBuilder template = UriBuilder.fromUri("http://localhost:80");

        if (mockServerContainer.getNetworkAliases().contains(ALIAS)) {
            config.put(APPLICATION_TEMPLATE_CONFIG, template.host(ALIAS).port(MockServerContainer.PORT).build().toString());
        } else {
            config.put(APPLICATION_TEMPLATE_CONFIG, template.port(mockServerContainer.getServerPort()).build().toString());
        }

        logger.infov("mock server started.");
        return config;
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
