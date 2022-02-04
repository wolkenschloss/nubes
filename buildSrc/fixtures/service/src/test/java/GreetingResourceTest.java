package family.haschka.wolkenschloss.convention.service;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import javax.ws.rs.core.Response;
import java.net.URL;

@QuarkusTest
public class GreetingResourceTest {
    @TestHTTPEndpoint(GreetingResource.class)
    @TestHTTPResource
    URL url;

    @Test
    public void getGreetingTest() {
        RestAssured.given()
                .when()
                .get(url)
                .then()
                .statusCode(Response.Status.OK.getStatusCode());
    }
}