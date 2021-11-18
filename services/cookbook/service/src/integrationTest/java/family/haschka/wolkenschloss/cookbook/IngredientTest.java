package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.testing.FileResource;
import family.haschka.wolkenschloss.cookbook.testing.MongoShell;
import family.haschka.wolkenschloss.cookbook.testing.MongoShellResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

@QuarkusIntegrationTest
@QuarkusTestResource(MongoShellResource.class)
@DisplayName("Ingredient CRUD Operations")
public class IngredientTest {

    MongoShell mongosh;

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
                    var location = String.format("http://localhost:%s/ingredient",
                            System.getProperty("quarkus.http.port"));

                    await()
                            .untilAsserted(() -> RestAssured.given()
                                    .when()
                                    .get(location)
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
                    .forEach(fixture -> {

                        var postRecipeUrl = String.format(
                                "http://localhost:%s/recipe",
                                System.getProperty("quarkus.http.port"));

                        RestAssured.given()
                                .body(fixture)
                                .contentType(ContentType.JSON)
                                .when()
                                .post(postRecipeUrl)
                                .then()
                                .statusCode(Response.Status.CREATED.getStatusCode());
                    });
        }
    }
}
