package familie.haschka.wolkenschloss.cookbook;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceConfigurableLifecycleManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class WiremockRecipes implements QuarkusTestResourceConfigurableLifecycleManager {

    private WireMockServer wireMockServer;

    @Override
    public Map<String, String> start() {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.options().usingFilesUnderClasspath("fixtures")
        );

        wireMockServer.start();

        try {
            stubFor(get(urlEqualTo("/lasagne.html"))
                    .willReturn(aResponse()
                            .withFixedDelay(3000)
                            .withStatus(200)
                            .withHeader("Content-Type", "text/html")
                            .withHeader("X-Files-Root", wireMockServer.getOptions().filesRoot().getPath())
                            .withBody(loadFixture("fixtures/lasagne.html"))
                            .withBodyFile("lasagne.html")));
        } catch (IOException e) {
            e.printStackTrace();
        }


        return Collections.singletonMap("family.haschka.wiremock.recipes", wireMockServer.baseUrl());
    }

    private String loadFixture(String filename) throws IOException {
        var url = getClass().getClassLoader().getResource(filename);
        return Files.readString(Path.of(url.getPath()));
    }

    @Override
    public void stop() {
        if(wireMockServer != null) {
            wireMockServer.stop();
        }
    }
}
