package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.ThrowingConsumer;

import javax.json.Json;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Stream;

import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@QuarkusIntegrationTest
@DisplayName("Recipe CRUD Operations")
public class RecipeTest {

    private static Set<String> mergeSet(Set<String> a, Set<String> b) {
        Set<String> result = new HashSet<>();
        Stream.of(a, b).forEach(result::addAll);

        return result;
    }

    @Test
    public void checkDefaultHttpPort() {
        var port = System.getProperty("quarkus.http.port");
        Assertions.assertEquals("9292", port);
    }

    @Test
    @DisplayName("Verbindungszeichenfolge darf nicht gesetzt sein")
    public void checkMongoDbConnectionString() {
        // Wenn Quarkus Dev Services verwendet werden, darf die Verbindungszeichenfolge
        // nicht gesetzt sein.
        var host = System.getProperty("quarkus.mongodb.connection-string");
        Assertions.assertNull(host);
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
                .post("recipe")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @TestFactory
    @DisplayName("POST /recipe")
    public Stream<DynamicTest> postRecipeTest() {
        return Stream.of(
                new PostRecipeTestcase("valid data",
                        "fixtures/schlammkrabbeneintopf.json",
                        Response.Status.CREATED.getStatusCode(),
                        response -> response.contentType(ContentType.JSON)
                                .header("Location", r -> equalTo(UriBuilder.fromUri(RestAssured.baseURI)
                                        .port(RestAssured.port)
                                        .path(RestAssured.basePath)
                                        .path("recipe")
                                        .path("{recipeId}")
                                        .build(r.path("_id").toString())
                                        .toString()))
                                .body("title", equalTo("Schlammkrabbeneintopf"))),

                // TODO: Es wäre schön, wenn der Fehler als JSON Objekt und nicht als HTML zurückkäme.
                new PostRecipeTestcase("invalid data",
                        "fixtures/invalid.json",
                        Response.Status.BAD_REQUEST.getStatusCode(),
                        response -> response.header("Location", equalTo(null)))
        ).map(testcase -> DynamicTest.dynamicTest(testcase.name,
                () -> testcase.assertions.accept(RestAssured
                        .given()
                        .body(readFixture(testcase.fixture))
                        .contentType(ContentType.JSON)
                        .when()
                        .post("recipe")
                        .then()
                        .statusCode(testcase.status))));
    }

    @TestFactory
    @DisplayName("GET /recipe/:id")
    public Stream<DynamicTest> getRecipeTest() throws URISyntaxException, IOException {
        return Stream.of(
                new GetRecipeTestcase("valid id",
                        location(createRecipe()),
                        response -> response.statusCode(Response.Status.OK.getStatusCode())
                                .contentType(ContentType.JSON)
                                .body("title", equalTo("Schlammkrabbeneintopf"))),

                new GetRecipeTestcase("unknown id",
                        UUID.randomUUID().toString(),
                        response -> response.statusCode(Response.Status.NOT_FOUND.getStatusCode())),

                new GetRecipeTestcase("malformed id",
                        "malformed id",
                        response -> response.statusCode(Response.Status.NOT_FOUND.getStatusCode()))
        ).map(testcase -> DynamicTest.dynamicTest(testcase.name, () -> {
            var response = RestAssured
                    .given()
                    .when()
                    .get(testcase.recipeId)
                    .then();

            testcase.assertions.accept(response);
        }));
    }

    @Test
    @DisplayName("GET /recipe")
    public void listRecipes() throws URISyntaxException, IOException {

        String id = recipeId(createRecipe());

        RestAssured
                .given()
                .when()
                .get("recipe")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
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
                .get("recipe")
                .jsonPath()
                .get("content");

        var allKeys = result.stream()
                .map(Map::keySet)
                .reduce(RecipeTest::mergeSet).orElseThrow();

        Assertions.assertEquals(allKeys, new HashSet<>(Arrays.asList("recipeId", "title")));
    }

    @Test
    @DisplayName("DELETE /recipe/:id")
    public void deleteRecipe() throws URISyntaxException, IOException {
        String location = location(createRecipe());

        // TODO: Ein Rezept löschen, dass nicht vorhanden ist.
        // Wenn ich ein Rezept lösche
        RestAssured
                .given()
                .when()
                .delete(location)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        // Dann kann ich es nicht mehr lesen
        RestAssured
                .given()
                .when()
                .get(location)
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @DisplayName("PUT /recipe/:id")
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
                        .statusCode(Response.Status.OK.getStatusCode())
                        .body("title", equalTo("Schlachterfischsuppe"));
            }
        }
    }

    @Test
    @DisplayName("PATCH /recipe/:id - change title")
    @Disabled("Does not work with reactive Server")
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
                .statusCode(Response.Status.OK.getStatusCode())
                .body("title", equalTo("Schneebeeren-Crostata"));


        // Dann werde ich den geänderten Titel beim nächsten Abrufen
        // des Rezepts erhalten.
        RestAssured
                .given()
                .accept(ContentType.JSON)
                .when()
                .get(location)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("title", equalTo("Schneebeeren-Crostata"));
    }

    public ValidatableResponse createRecipe() throws URISyntaxException, IOException {
        var str = readFixture("fixtures/schlammkrabbeneintopf.json");

        // POST /recipe valid data
        return RestAssured
                .given()
                .log().all()
                .body(str)
                .contentType(ContentType.JSON)
                .when()
                .post("recipe")
                .then()
                .log().all()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", response -> equalTo(UriBuilder.fromUri(RestAssured.baseURI)
                        .port(RestAssured.port)
                        .path(RestAssured.basePath)
                        .path("recipe")
                        .path("{recipeId}")
                        .build(response.path("_id").toString())
                        .toString()));
    }

    private String location(ValidatableResponse response) {
        return response.extract().header("Location");
    }

    private String recipeId(ValidatableResponse response) {
        return response.extract().path("_id");
    }

    private String readFixture(String resourceFileName) throws URISyntaxException, IOException {
        URL resource = getClass().getClassLoader().getResource(resourceFileName);

        if (resource == null) {
            throw new FileNotFoundException(resourceFileName);
        }

        File file = new File(resource.toURI());
        return Files.readString(file.toPath());
    }

    record PostRecipeTestcase(String name, String fixture, int status,
                              ThrowingConsumer<ValidatableResponse> assertions) {
    }

    public static class GetRecipeTestcase {
        String name;
        String recipeId;
        ThrowingConsumer<ValidatableResponse> assertions;

        public GetRecipeTestcase(
                String name,
                String recipeId,
                ThrowingConsumer<ValidatableResponse> assertions) {
            this.name = name;
            this.recipeId = recipeId;
            this.assertions = assertions;
        }
    }
}
