package family.haschka.wolkenschloss.convention.service;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import javax.ws.rs.core.Response;

@QuarkusIntegrationTest
public class GreetingTest {

    @Test
    public void getGreeting() {
        String port = System.getProperty("quarkus.http.port");
        String url = String.format("http://localhost:%s/greeting", port);

        RestAssured.given()
                .when()
                .get(url)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }
}