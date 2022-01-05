package family.haschka.wolkenschloss.cookbook.recipe;

import com.google.inject.Inject;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import org.jsoup.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.json.bind.Jsonb;
import javax.ws.rs.core.Response;
import java.net.URL;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class GroupResourceTest {

    @TestHTTPEndpoint(GroupsResource.class)
    @TestHTTPResource
    URL url;

    @Inject
    Jsonb jsonb;

    @BeforeEach
    public void configureObjectMapper() {
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(objectMapperConfig().jsonbObjectMapperFactory(
                        (cls, charset) -> jsonb
                ));
    }

    @ParameterizedTest
    @DisplayName("GET /units/groups")
    @ValueSource(strings = {"Volumes", "Weights", "Kitchen terms", "Common Terms"})
    void listTest(String group) {
        RestAssured.given()
                .when()
                .get(url)
                .then()
                .log().all()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("groups.name", hasItem(group));
    }
}
