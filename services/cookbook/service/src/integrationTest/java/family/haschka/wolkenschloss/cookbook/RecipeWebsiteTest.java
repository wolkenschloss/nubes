package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.testing.MockServerResource;
import family.haschka.wolkenschloss.cookbook.testing.RecipeWebsite;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.UriBuilder;

@QuarkusIntegrationTest
public class RecipeWebsiteTest {

    @Test
    public void orderUriHasHttpScheme() {
        UriBuilder builder = RecipeWebsite.orderUriFrom("__files/{filename}");
        var uri = builder.build("xyz");

        Assertions.assertEquals(RecipeWebsite.SCHEME, uri.getScheme());
        Assertions.assertEquals(
                String.format("http://%s:%s/__files/xyz",
                    System.getProperty(MockServerResource.SERVER_HOST_CONFIG),
                        System.getProperty(MockServerResource.SERVER_PORT_CONFIG)), uri.toString());
    }

    @Test
    public void orderUriHasCorrectHost() {
        UriBuilder builder = RecipeWebsite.orderUriFrom("__files/{filename}");
        var uri = builder.build("xyz");

        Assertions.assertEquals(System.getProperty(MockServerResource.SERVER_HOST_CONFIG), uri.getHost());
    }
}
