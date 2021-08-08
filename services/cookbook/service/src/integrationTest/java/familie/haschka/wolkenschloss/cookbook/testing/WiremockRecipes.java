package familie.haschka.wolkenschloss.cookbook.testing;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

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


        return Collections.singletonMap("family.haschka.wiremock.recipes", wireMockServer.baseUrl());
    }

    @Override
    public void stop() {
        if(wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
