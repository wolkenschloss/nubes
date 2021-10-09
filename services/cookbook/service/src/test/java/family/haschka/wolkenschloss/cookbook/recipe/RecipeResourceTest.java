package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

@QuarkusTest
public class RecipeResourceTest {

    @InjectMock
    RecipeService service;

    @TestHTTPEndpoint(RecipeResource.class)
    @TestHTTPResource
    URL url;

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
    void searchTest() throws URISyntaxException, MalformedURLException {

        var recipes = new ArrayList<Summary>();
        recipes.add(new Summary(UUID.randomUUID(), "Blaukraut"));
        var toc = new TableOfContents(1, recipes);

        Mockito.when(service.list(0, 4, null))
                .thenReturn(Uni.createFrom().item(toc));

        var query = UriBuilder.fromUri(url.toURI())
                .queryParam("from", 0)
                .queryParam("to", 4)
                .build()
                .toURL();

        RestAssured.given()
                .when()
                .accept(MediaType.APPLICATION_JSON)
                .get(query)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content.size()", is(recipes.size()));

        Mockito.verify(service, Mockito.times(1)).list(0, 4, null);
    }

    @Test
    @DisplayName("POST /recipe")
    void postRecipeTest() {

        var recipe = RecipeFixture.LASAGNE.get();
        var id = UUID.randomUUID();
        recipe.servings = new Servings(1);

        Mockito.when(creator.save(recipe))
                .thenReturn(Uni.createFrom().item(RecipeFixture.LASAGNE.withId(id)));

        RestAssured.given()
                .body(recipe, ObjectMapperType.JSONB)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .post(url)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", response -> {
                    var expected = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();
                    return equalTo(expected.toString());
                });

        Mockito.verify(creator, Mockito.times(1)).save(recipe);
        Mockito.verifyNoMoreInteractions(service);
        Mockito.verifyNoMoreInteractions(creator);
    }

    @Test
    @DisplayName("GET /recipe/{id}")
    public void getRecipe() throws URISyntaxException {
        var recipe = RecipeFixture.LASAGNE.withId(UUID.randomUUID());
        var query = UriBuilder.fromUri(url.toURI()).path(recipe.recipeId.toString()).build();

        Mockito.when(service.get(recipe.recipeId, Optional.empty()))
                .thenReturn(Uni.createFrom().item(Optional.of(recipe)));

        System.out.println(RecipeFixture.LASAGNE.asJson(jsonb));
        RestAssured.given()
                .accept(ContentType.JSON)
                .when()
                .get(query)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is(jsonb.toJson(recipe)));

        Mockito.verify(service, Mockito.times(1)).get(recipe.recipeId, Optional.empty());
    }

    @Test
    @DisplayName("DELETE /recipe/{id}")
    public void deleteRecipe() throws URISyntaxException {
        var id = UUID.randomUUID();
        var query = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();
        RestAssured.given()
                .when()
                .delete(query)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Mockito.verify(service, Mockito.times(1)).delete(id);
    }

    @Test
    @DisplayName("PUT /recipe/{id}")
    public void putRecipe() throws URISyntaxException {
        var recipe = RecipeFixture.LASAGNE.withId(UUID.randomUUID());
        var query = UriBuilder.fromUri(url.toURI()).path(recipe.recipeId.toString()).build();

        RestAssured.given()
                .body(recipe, ObjectMapperType.JSONB)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .put(query)
                .then()
                .statusCode(Response.Status.NO_CONTENT.getStatusCode());

        Mockito.verify(service, Mockito.times(1)).update(recipe);
    }
}
