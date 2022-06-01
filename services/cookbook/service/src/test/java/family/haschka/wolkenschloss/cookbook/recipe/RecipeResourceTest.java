package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.ArrayList;
import java.util.Optional;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@DisplayName("Recipe Resource")
@TestHTTPEndpoint(RecipeResource.class)
public class RecipeResourceTest {

    @InjectMock
    RecipeService service;

    @Inject
    Jsonb jsonb;

    @InjectMock
    CreatorService creator;

    @BeforeEach
    public void configureObjectMapper() {
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(objectMapperConfig().jsonbObjectMapperFactory(
                        (cls, charset) -> jsonb
                ));
    }

    @AfterEach
    public void verifyMocks() {
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /recipe?from=x&to=y")
    void searchTest() {

        var recipes = new ArrayList<Summary>();
        recipes.add(new Summary(ObjectId.get().toHexString(), "Blaukraut"));
        var toc = new TableOfContents(0, recipes);

        Mockito.when(service.list(0, 4, null))
                .thenReturn(Uni.createFrom().item(toc));

        RestAssured.given()
                .when()
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("from", 0)
                .queryParam("to", 4)
                .get()
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content.size()", is(recipes.size()));

        Mockito.verify(service, Mockito.times(1)).list(0, 4, null);
    }


    @Test
    @DisplayName("Test equality")
    public void equalityTest() {
        var id = ObjectId.get().toHexString();
        var recipe1 = RecipeFixture.LASAGNE.withId(id);
        var recipe2 = RecipeFixture.LASAGNE.withId(id);

        Assertions.assertEquals(recipe1, recipe2);
    }

    @Test
    @DisplayName("POST /recipe")
    void postRecipeTest() {

        var recipe = RecipeFixture.LASAGNE.get();
        var id = ObjectId.get().toHexString();

        var recipeWithId = RecipeFixture.LASAGNE.withId(id);
        Mockito.when(creator.save(any(Recipe.class)))
                .thenReturn(Uni.createFrom().item(recipeWithId));

        RestAssured.given()
                .log().all()
                .body(recipe, ObjectMapperType.JSONB)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", response -> equalTo(UriBuilder.fromUri(RestAssured.baseURI)
                        .port(RestAssured.port)
                        .path(RestAssured.basePath)
                        .path(id)
                        .build()
                        .toString()));

        Mockito.verify(creator, Mockito.times(1)).save(recipe);
        Mockito.verifyNoMoreInteractions(service);
        Mockito.verifyNoMoreInteractions(creator);
    }

    @Test
    @DisplayName("GET /recipe/{id}")
    public void getRecipe() {
        var recipe = RecipeFixture.LASAGNE.withId(ObjectId.get().toHexString());

        Mockito.when(service.get(recipe._id(), Optional.empty()))
                .thenReturn(Uni.createFrom().item(Optional.of(recipe)));

        System.out.println(RecipeFixture.LASAGNE.asJson(jsonb));
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get(recipe._id())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is(jsonb.toJson(recipe)));

        Mockito.verify(service, Mockito.times(1)).get(recipe._id(), Optional.empty());
    }

    @Test
    @DisplayName("DELETE /recipe/{id}")
    public void deleteRecipe() {
        var id = ObjectId.get();
        RestAssured.given()
                .when()
                .delete(id.toString())
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Mockito.verify(service, Mockito.times(1)).delete(id.toHexString());
    }

    @Test
    @DisplayName("PUT /recipe/{id}")
    public void putRecipe() {
        var recipe = RecipeFixture.LASAGNE.withId(ObjectId.get().toHexString());

        RestAssured.given()
                .body(recipe, ObjectMapperType.JSONB)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .put(recipe._id())
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Mockito.verify(service, Mockito.times(1)).update(recipe);
    }
}
