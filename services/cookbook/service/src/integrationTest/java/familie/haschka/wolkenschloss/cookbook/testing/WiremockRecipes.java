package familie.haschka.wolkenschloss.cookbook.testing;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.eclipse.microprofile.config.ConfigProvider;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WiremockRecipes implements QuarkusTestResourceLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options()
                    .fileSource(new ClasspathFileSourceWithoutLeadingSlash())
        );

        wireMockServer.start();

        stubFor(get(urlEqualTo("/lasagne.html"))
                .willReturn(aResponse()
                        .withFixedDelay(3000)
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

        var host = ConfigProvider.getConfig().getValue("wolkenschloss.nubes.testhost", String.class);

        return Collections.singletonMap("family.haschka.wiremock.recipes",
                wireMockServer.baseUrl().replace("localhost", host));
    }

    @Override
    public void stop() {
        if(wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
