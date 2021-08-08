package familie.haschka.wolkenschloss.cookbook;

import familie.haschka.wolkenschloss.cookbook.testing.MongoDbResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
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
import java.util.*;
import java.util.stream.Stream;

import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusIntegrationTest
@QuarkusTestResource(value = MongoDbResource.class)
@DisplayName("Recipe CRUD Operations")
public class RecipeTest {

    private static final Logger logger = LoggerFactory.getLogger(RecipeTest.class);

    @Test
    public void checkDefaultHttpPort() {
        var port = System.getProperty("quarkus.http.port");
        logger.info("quarkus.http.port = {}", port);
        Assertions.assertEquals("9292", port);
    }

    @Test
    @DisplayName("Verbindungszeichenfolge erfordert UUID Konfiguration")
    public void checkMongoDbConnectionString() {
        var host = System.getProperty("quarkus.mongodb.connection-string");
        logger.warn("quarkus.mongodb.hosts = {}", host);
        Assertions.assertNotNull(host);
        assertThat(host, containsString("uuidRepresentation=STANDARD"));
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
    public void readRecipe() throws URISyntaxException, IOException {

        RestAssured
                .given()
                .when()
                .get(location(createRecipe()))
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

    private String recipeId(ValidatableResponse response) {
        return response.extract().path("recipeId");
    }
    @Test
    @DisplayName("GET /recipe")
    public void listRecipes() throws URISyntaxException, IOException {

        String id = recipeId(createRecipe());

        RestAssured
                .given()
                .when()
                .get(getUrl())
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("content.size()", greaterThan(0))
                .body("content.size()", response -> equalTo(response.getBody().jsonPath().<Integer>get("total")))
                .body("content.find {it.recipeId == '%s'}.title",
                        withArgs(id),
                        equalTo("Schlammkrabbeneintopf"));
    }

    @Test
    @DisplayName("GET /recipe minimal data")
    public void listRecipesMinimal() throws URISyntaxException, IOException {

        createRecipe();

        ArrayList<Map<String, Object>> result = RestAssured.given()
                .when()
                .get(getUrl())
                .jsonPath()
                .get("content");

        var allKeys = result.stream()
                .map(Map::keySet)
                .reduce(RecipeTest::mergeSet).orElseThrow();

        Assertions.assertEquals(allKeys, new HashSet<>(Arrays.asList("recipeId", "title")));
    }

    @Test
    @DisplayName("Delete Recipe")
    public void deleteRecipe() throws URISyntaxException, IOException {
        String location = location(createRecipe());

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
    public void updateRecipe() throws URISyntaxException, IOException {
        var created = createRecipe();
        var location = created.extract().header("Location");

        try (var body = created.extract().body().asInputStream()) {
            try (var reader = Json.createReader(body)) {
                var recipe = reader.readObject();
                var changedRecipe = Json.createObjectBuilder(recipe);
                changedRecipe.remove("title");
                changedRecipe.add("title", "Schlachterfischsuppe");

                RestAssured
                        .given()
                        .body(changedRecipe.build().toString())
                        .contentType(ContentType.JSON)
                        .when()
                        .put(location)
                        .then()
                        .statusCode(HttpStatus.SC_OK)
                        .body("title", equalTo("Schlachterfischsuppe"));
            }
        }
    }

    @Test
    @DisplayName("PATCH /recipe/:id - change title")
    public void patchRecipe() throws URISyntaxException, IOException {

        String location = location(createRecipe());

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
        // des Rezepts erhalten.
        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("title", equalTo("Schneebeeren-Crostata"));
    }

    private String getPort() {
        return System.getProperty("quarkus.http.port");
    }

    private String getUrl() {
        return "http://localhost:" + getPort() + "/recipe";
    }

    public ValidatableResponse createRecipe() throws URISyntaxException, IOException {
        var str = readFixture("fixtures/schlammkrabbeneintopf.json");

        // POST /recipe valid data
        return RestAssured
                .given()
                .body(str)
                .contentType(ContentType.JSON)
                .when()
                .post(getUrl())
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", response -> equalTo(getUrl() + "/" + response.path("recipeId")));
    }

    private String location(ValidatableResponse response) {
        return response.extract().header("Location");
    }

    private String readFixture(String resourceFileName) throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource(resourceFileName);

        if (resource == null) {
            throw new FileNotFoundException(resourceFileName);
        }

        File file = new File(resource.toURI());
        return Files.readString(file.toPath());
    }

    private static Set<String> mergeSet(Set<String> a, Set<String> b) {
        Set<String> result = new HashSet<>();
        Stream.of(a, b).forEach(result::addAll);

        return result;
    }
}
