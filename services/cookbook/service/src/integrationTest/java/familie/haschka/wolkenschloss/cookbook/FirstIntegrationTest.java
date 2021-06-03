package familie.haschka.wolkenschloss.cookbook;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusIntegrationTest
@QuarkusTestResource(value = MongoDbResource.class, restrictToAnnotatedClass = true)
@DisplayName("Recipe CRUD Operations")
public class FirstIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(FirstIntegrationTest.class);

    @Test
    public void checkDefaultHttpPort() {
        var port = System.getProperty("quarkus.http.port");
        logger.info("quarkus.http.port = {}", port);
        Assertions.assertEquals("8081", port);
    }

    @Test
    public void checkMongoDbHostConfiguration() {
        var host = System.getProperty("quarkus.mongodb.hosts");
        logger.warn("quarkus.mongodb.hosts = {}", host);
        Assertions.assertNotNull(host);
    }

    private ValidatableResponse response;

    private String getPort() {
        return System.getProperty("quarkus.http.port");
    }

    private String getUrl() {
        return "http://localhost:" + getPort() + "/recipe";
    }

    @BeforeEach
    public void createRecipe() {

        var recipe = "{\"title\": \"Schlammkrabbeneintopf\", \"herstellung\": \"Bekannt.\"}";

        response = RestAssured
                .given()
                    .body(recipe)
                    .contentType(ContentType.JSON)
                .when()
                    .post(getUrl())
                .then()
                    .statusCode(HttpStatus.SC_CREATED)
                    .header("Location", response -> equalTo(getUrl() + "/" + response.path("_id")));
    }

    @Test
    @DisplayName("Read Recipe")
    public void readRecipe() {
        var recipe = "{\"title\": \"Schlammkrabbeneintopf\", \"herstellung\": \"Bekannt.\"}";

        // TODO:
        // 1. Aus location die ID ermitteln
        // 2. GET /recipe liefert alle Recipes. Darin sollte ID enthalten sein.
        // 3. GET /recipe/ID liefert das gespeicherte Recipe zurück.
        // GET /recipe/id
        RestAssured
                .given()
                .when()
                    .get(response.extract().header("Location"))
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("title", equalTo("Schlammkrabbeneintopf"))
                    .body("herstellung", equalTo("Bekannt."));
    }

    @Test
    @DisplayName("Read all Recipes")
    public void listRecipes() {

        String id = response.extract().path("_id");

        RestAssured
                .given()
                .when()
                    .get(getUrl())
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("size()", greaterThan(0))
                    .body("find {it._id == \"" + id + "\"}.title", equalTo("Schlammkrabbeneintopf"));
    }

    @Test
    @DisplayName("Delete Recipe")
    public void deleteRecipe() {
        String location = response.extract().header("Location");

        // TODO: Ein Rezept löschen, dass nicht vorhanden ist.
        // Wenn ich ein Rezept lösche
        RestAssured
                .given()
                .when()
                    .delete(location)
                .then()
                    .statusCode(HttpStatus.SC_NO_CONTENT);

        // Dann kann ich es nicht mehr lesen
        RestAssured
                .given()
                .when()
                    .get(location)
                .then()
                    .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("Update Recipe")
    public void updateRecipe() {
        String location = response.extract().header("Location");
        var body = response.extract().body().asInputStream();
        var reader = Json.createReader(body);
        var recipe = reader.readObject();
        var change = Json.createObjectBuilder(recipe);
        change.remove("title");
        change.add("title", "Schlachterfischsuppe");

        var changed = change.build();

        RestAssured
                .given()
                    .body(changed.toString())
                    .contentType(ContentType.JSON)
                .when()
                    .put(location)
                .then()
                    .statusCode(HttpStatus.SC_OK)
                    .body("title", equalTo("Schlachterfischsuppe"));
    }
}
