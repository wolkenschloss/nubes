package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.testing.FileResource;
import family.haschka.wolkenschloss.cookbook.testing.MongoShell;
import family.haschka.wolkenschloss.cookbook.testing.MongoShellResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.*;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

// @Blueprint(tags = {"integration test", "multiple endpoints", "mongodb")
@QuarkusIntegrationTest
@QuarkusTestResource(MongoShellResource.class)
@DisplayName("Ingredient CRUD Operations")
public class IngredientTest {

    MongoShell mongosh;

    @Test
    @DisplayName("Verify RestAssured settings")
    public void verifyRestAssured() throws URISyntaxException {
        var expected = UriBuilder.fromUri(RestAssured.baseURI)
                .port(RestAssured.port)
                .path(RestAssured.basePath)
                .build();

        Assertions.assertEquals(expected, new URI("http://localhost:9292/cookbook"));
    }

    @TestFactory
    @DisplayName("GET /ingredients")
    public Stream<DynamicTest> listIngredientsOneElement() {

        return Stream.of(
                        new IngredientTestCase(
                                "empty",
                                List.of(),
                                emptyIterable()),
                        new IngredientTestCase(
                                "overlapping ingredient list",
                                List.of("tomatosoup.json", "fishsoup.json"),
                                contains("Fish", "Pepper", "Salt", "Tomato")),
                        new IngredientTestCase(
                                "list of ingredients without overlap",
                                List.of("tomatosoup.json", "steak.json"),
                                contains("Chili oil", "Pepper", "Salt", "Steak", "Tomato")))
                .map(testcase -> DynamicTest.dynamicTest(testcase.name(), () -> {
                    mongosh.eval("db.dropDatabase();").verify();
                    testcase.given();

                    await()
                            .untilAsserted(() -> RestAssured.given()
                                    .when()
                                    .get("ingredient")
                                    .then()
                                    .statusCode(Response.Status.OK.getStatusCode())
                                    .body("content.name", testcase.expect()));
                }));
    }

    public record IngredientTestCase(String name, List<String> recipes, Matcher<Iterable<? extends String>> expect) {
        public void given() {
            recipes.stream()
                    .map(recipe -> String.format("fixtures/%s", recipe))
                    .map(path -> new FileResource(path).read())
                    .forEach(fixture -> RestAssured.given()
                            .body(fixture)
                            .contentType(ContentType.JSON)
                            .when()
                            .post("recipe")
                            .then()
                            .statusCode(Response.Status.CREATED.getStatusCode()));
        }
    }
}
