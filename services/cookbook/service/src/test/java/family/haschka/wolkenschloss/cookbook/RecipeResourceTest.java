package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class RecipeResourceTest {

    @InjectMock
    RecipeService service;

    @TestHTTPEndpoint(RecipeResource.class)
    @TestHTTPResource
    URL url;

    @Test
    void searchTest() {

        var recipes = new ArrayList<BriefDescription>();
        recipes.add(new BriefDescription(UUID.randomUUID(), "Blaukraut"));
        var toc = new TableOfContents(1, recipes);

        Mockito.when(service.list(0, 4)).thenReturn(toc);

        RestAssured.given()
                .when().get(url)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("contents.size()", is(recipes.size()));

        Mockito.verify(service, Mockito.times(1)).list(0, 4);
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    void postRecipeTest() {

        var recipe = new Recipe("Schrankmuscheleintopf", "Das gibt es nicht");
        var id = UUID.randomUUID();

        Mockito.doAnswer((x) -> {
            x.getArgument(0, Recipe.class).recipeId = id;
            return null;
        }).when(service).save(any(Recipe.class));

        RestAssured.given().body(recipe).contentType(MediaType.APPLICATION_JSON)
                .when().post(url)
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
