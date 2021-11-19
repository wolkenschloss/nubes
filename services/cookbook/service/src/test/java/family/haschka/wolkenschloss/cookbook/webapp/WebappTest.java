package family.haschka.wolkenschloss.cookbook.webapp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;

@QuarkusTest
public class WebappTest {

    @Test
    public void checkRoot() {
        RestAssured.given()
                .when().get("/")
                .then().log().all()
                .statusCode(Response.Status.OK.getStatusCode());
    }
}
