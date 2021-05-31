package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;

@QuarkusTest
@TestHTTPEndpoint(RecipeRessource.class)
public class RecipeTests {

    @Test
    void searchTest() {
        given()
            .when().get()
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("hello"));
    }
}
