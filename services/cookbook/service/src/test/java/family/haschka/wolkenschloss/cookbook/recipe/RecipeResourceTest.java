package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.path.json.mapper.factory.JsonbObjectMapperFactory;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import static io.restassured.config.ObjectMapperConfig.objectMapperConfig;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class RecipeResourceTest {

    @InjectMock
    RecipeService service;

    @TestHTTPEndpoint(RecipeResource.class)
    @TestHTTPResource
    URL url;

    @Inject
    Jsonb jsonb;

    @BeforeEach
    public void configureObjectMapper() {
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(objectMapperConfig().jsonbObjectMapperFactory(
                        (cls, charset) -> jsonb
                ));
    }

    @Test
    void searchTest() throws URISyntaxException, MalformedURLException {

        var recipes = new ArrayList<Summary>();
        recipes.add(new Summary(UUID.randomUUID(), "Blaukraut"));
        var toc = new TableOfContents(1, recipes);

        Mockito.when(service.list(0, 4, null)).thenReturn(toc);

        var query = UriBuilder.fromUri(url.toURI())
                .queryParam("from", 0)
                        .queryParam("to", 4)
                                .build().toURL();
        RestAssured.given()
                .log().all()
                .when().get(query)

                .then()
                .log().all()
                .statusCode(HttpStatus.SC_OK)
                .body("content.size()", is(recipes.size()));

        Mockito.verify(service, Mockito.times(1)).list(0, 4, null);
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    void postRecipeTest() {

        var recipe = new Recipe("Schrankmuscheleintopf", "Das gibt es nicht");
        var id = UUID.randomUUID();
        recipe.servings = new Servings(1);

        Mockito.doAnswer((x) -> {
            x.getArgument(0, Recipe.class).recipeId = id;
            return null;
        }).when(service).save(any(Recipe.class));

        RestAssured.given()
                .body(recipe, ObjectMapperType.JSONB)
                .contentType(MediaType.APPLICATION_JSON)
                .log().all()
                .when()
                .post(url)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", response -> {
                            var expected = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();
                            return org.hamcrest.Matchers.equalTo(expected.toString());
                        });

        Mockito.verify(service, Mockito.times(1)).save(Mockito.any(Recipe.class));
        Mockito.verifyNoMoreInteractions(service);
    }
}