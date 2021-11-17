package familie.haschka.wolkenschloss.cookbook;

import familie.haschka.wolkenschloss.cookbook.testing.MongoShellResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.jboss.logging.Logger;
import org.junit.Assume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;

import static org.hamcrest.Matchers.*;

@QuarkusIntegrationTest
@QuarkusTestResource(MongoShellResource.class)
@DisplayName("Ingredient CRUD Operations")

public class IngredientTest  {

    private static final Logger LOG = org.jboss.logging.Logger.getLogger(IngredientTest.class);

    MongoShellResource.MongoShell mongosh;

    @BeforeEach
    public void dropDatabaseCollections() throws IOException, InterruptedException {
        MongoShellResource.MongoShell.log(
                mongosh.eval("disableTelemetry(); db.getCollectionNames().join(', ');"));

        MongoShellResource.MongoShell.log(
                mongosh.eval("disableTelemetry(); db.Ingredient.drop(); db.Recipe.drop();")
        );

//        Assume.assumeThat(
//                "ingredient collection must be dropped",
//                mongosh.eval("disableTelemetry(); db.Ingredient.drop(); db.Recipe.drop();").getExitCode(),
//                equalTo(0));

        MongoShellResource.MongoShell.log(
                mongosh.eval("disableTelemetry(); db.getCollectionNames().join(', ');"));
    }

    @Test
    @DisplayName("GET /ingredients (empty)")
    public void listIngredientsEmpty() throws InterruptedException {
        var location = String.format("http://localhost:%s/ingredient",
                System.getProperty("quarkus.http.port"));

        RestAssured.given()
                .when()
                .get(location)
                .then()
                .log().all()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content.name", empty());
    }


    public record IngredientTestCase(String name, List<String> recipes, String[] content) {
        public void given() {
            recipes.stream()
                    .map(recipe -> readFixture(String.format("fixtures/%s", recipe)))
                    .forEach(fixture -> {

                var post_recipe_url = String.format("http://localhost:%s/recipe", System.getProperty("quarkus.http.port"));

                RestAssured.given()
                        .body(fixture)
                        .contentType(ContentType.JSON)
                        .when()
                        .post(post_recipe_url)
                        .then()
                        .statusCode(Response.Status.CREATED.getStatusCode());
            });
        }

        public int expectedCount() {
            return content.length;
        }

        private String readFixture(String resourceFileName) {
            try {
                URL resource = getClass().getClassLoader().getResource(resourceFileName);

                if (resource == null) {
                    throw new FileNotFoundException(resourceFileName);
                }

                File file = new File(resource.toURI());
                return Files.readString(file.toPath());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test
    @DisplayName("GET /ingredients (1 Element)")
    public void listIngredientsOneElement() {

        var testcase = new IngredientTestCase(
                "1 Recipe",
                List.of("schlammkrabbeneintopf.json"),
                new String[] {"Frostmirriam", "Pfeffer", "Salz", "Schlammkrabbenchitin"}
                );

        testcase.given();

        var location = String.format("http://localhost:%s/ingredient",
                System.getProperty("quarkus.http.port"));

        RestAssured.given()
                .when()
                .get(location)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("count", equalTo(testcase.expectedCount()))
                .body("content.name",
                        contains("Frostmirriam", "Pfeffer", "Salz", "Schlammkrabbenchitin"))
                .extract()
                .jsonPath()
                .get("");
    }
}
