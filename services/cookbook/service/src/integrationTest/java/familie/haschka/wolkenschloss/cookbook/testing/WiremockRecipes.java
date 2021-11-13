package familie.haschka.wolkenschloss.cookbook.testing;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class WiremockRecipes implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {

    private WireMockServer wireMockServer;

    private static Logger logger = Logger.getLogger(WiremockRecipes.class);

    @Override
    public Map<String, String> start() {

        System.out.println("Starting Wiremock Server");
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                        .fileSource(new ClasspathFileSourceWithoutLeadingSlash())

        );

        wireMockServer.start();

        stubFor(get(urlEqualTo("/lasagne.html"))
                .willReturn(aResponse()
                        .withFixedDelay(500)
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withHeader("X-Files-Root", wireMockServer.getOptions().filesRoot().getPath())
                        .withBodyFile("lasagne.html")));

        stubFor(get(urlEqualTo("/news.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBodyFile("news.html")));

        stubFor(get(urlEqualTo("/menu.html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBodyFile("menu.html")));

        try {
            return Collections.singletonMap(
                    "family.haschka.wiremock.recipes",
                    wireMockServer.baseUrl().replace("localhost", IpUtil.getHostAddress()));
        } catch (UnknownHostAddress e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {

        context.devServicesProperties().forEach((k, v) -> {
            logger.infov("[{0}]: {1}", k, v);
        });
    }
}
