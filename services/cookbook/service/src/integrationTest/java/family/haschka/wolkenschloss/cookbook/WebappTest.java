package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

@QuarkusIntegrationTest
public class WebappTest {

    @Test
    public void webappShouldLoad() {
        RestAssured.given().when().get("/")
                .then()
                .log().all()
                .statusCode(Response.Status.OK.getStatusCode());
    }
}
