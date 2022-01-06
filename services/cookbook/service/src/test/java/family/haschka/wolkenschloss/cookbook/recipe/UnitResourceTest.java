package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.annotation.JsonbCreator;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Stream;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;

@QuarkusTest
public class UnitResourceTest {

    @TestHTTPEndpoint(UnitsResource.class)
    @TestHTTPResource
    URL url;

    public record Item(String name, String[] values) {
        @JsonbCreator
        public Item {}
    }

    @Inject
    Jsonb jsonb;

    @BeforeEach
    public void configureObjectMapper() {
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(objectMapperConfig().jsonbObjectMapperFactory(
                        (cls, charset) -> jsonb
                ));
    }

    @ParameterizedTest(name = "[{index}] GET /units should contain {0}")
    @DisplayName("GET /units")
    @EnumSource
    void listAllUnits(Unit unit) {

        var items = RestAssured.given()
                .when()
                .get(url)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().body().as(Item[].class);

        Assertions.assertTrue(Arrays.stream(items).anyMatch(i -> i.name.equals(unit.name())));
    }

    @ParameterizedTest(name = "[{index}] GET /units should contain item with alias {0}")
    @MethodSource("aliases")
    public void listAllAliases(String alias) {
        var items = RestAssured.given()
                .when()
                .get(url)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().body().as(Item[].class);

        Assertions.assertTrue(
                Arrays.stream(items).anyMatch(i -> Arrays.asList(i.values).contains(alias)));
    }

    private static Stream<String> aliases() {
        return Unit.stream();
    }
}
