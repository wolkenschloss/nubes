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
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusIntegrationTest
@QuarkusTestResource(value = MongoDbResource.class, restrictToAnnotatedClass = true)
@DisplayName("Recipe CRUD Operations")
public class RecipeTest {

    private static final Logger logger = LoggerFactory.getLogger(RecipeTest.class);

    @Test
    public void checkDefaultHttpPort() {
        var port = System.getProperty("quarkus.http.port");
        logger.info("quarkus.http.port = {}", port);
        Assertions.assertEquals("8081", port);
    }

    @Test
    @DisplayName("Verbindungszeichenfolge erfordert UUID Konfiguration")
    public void checkMongoDbConnectionString() {
        var host = System.getProperty("quarkus.mongodb.connection-string");
        logger.warn("quarkus.mongodb.hosts = {}", host);
        Assertions.assertNotNull(host);
        assertThat(host, containsString("uuidRepresentation=STANDARD"));
    }

    private ValidatableResponse response;

    private String getPort() {
        return System.getProperty("quarkus.http.port");
    }

    private String getUrl() {
        return "http://localhost:" + getPort() + "/recipe";
    }

    @BeforeEach
    public void createRecipe() throws URISyntaxException, IOException {
        var str = readFixture("fixtures/schlammkrabbeneintopf.json");

        // POST /recipe valid data
        response = RestAssured
                .given()
                .body(str)
                .contentType(ContentType.JSON)
                .when()
                .post(getUrl())
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", response -> equalTo(getUrl() + "/" + response.path("recipeId")));
    }

    @Test
    @DisplayName("POST /recipe invalid data => 400")
    public void testCreateInvalid() throws URISyntaxException, IOException {
        var str = readFixture("fixtures/invalid.json");

        RestAssured
                .given()
                .body(str)
                .contentType(ContentType.JSON)
                .when()
                .post(getUrl())
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("GET /recipe/:id")
    public void readRecipe() {

        RestAssured
                .given()
                .when()
                .get(response.extract().header("Location"))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Schlammkrabbeneintopf"))
                .body("ingredients.size", equalTo(4))
                .body("preparation", equalTo("Bekannt."));
    }

    @Test
    @DisplayName("GET /recipe/:id invalid id")
    public void readRecipeInvalidId() {

        var url = URI.create(getUrl());

        // UriBuild.path ersetzt nicht, sondern fügt hinzu
        var uriWithInvalidId = UriBuilder.fromUri(url)
                .path(UUID.randomUUID().toString())
                .build();

        Assertions.assertFalse(uriWithInvalidId.toString().startsWith("http://localhost:8081/recipe/recipe"));

        RestAssured
                .given()
                .when()
                .get(uriWithInvalidId)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("GET /recipe")
    public void listRecipes() {

        String id = response.extract().path("recipeId");

        RestAssured
                .given()
                .when()
                .get(getUrl())
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", greaterThan(0))
                .body("find {it.recipeId == \"" + id + "\"}.title", equalTo("Schlammkrabbeneintopf"));
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

    @Test
    @DisplayName("PATCH /recipe/:id - change title")
    public void patchRecipe() {

        String location = response.extract().header("Location");

        Map<String, ?> config = Collections.emptyMap();
        var factory = Json.createBuilderFactory(config);

        var patches = factory.createArrayBuilder()
                .add(factory.createObjectBuilder()
                        .add("op", "replace")
                        .add("path", "/title")
                        .add("value", "Schneebeeren-Crostata"))
                .build();

        // Wenn ich den Titel des Rezeptes ändere
        RestAssured
                .given()
                .contentType("application/json-patch+json;charset=utf-8")
                .body(patches.toString())
                .when()
                .patch(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Schneebeeren-Crostata"));


        // Dann werde ich den geänderten Titel beim nächsten Abrufen
        // des Rezept erhalten.
        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Schneebeeren-Crostata"));
    }

    private String readFixture(String resourceFileName) throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource(resourceFileName);

        if (resource == null) {
            throw new FileNotFoundException(resourceFileName);
        }

        File file = new File(resource.toURI());
        return Files.readString(file.toPath());
    }
}
