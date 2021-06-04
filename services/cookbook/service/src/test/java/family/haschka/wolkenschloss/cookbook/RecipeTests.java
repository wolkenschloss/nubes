package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.bson.types.ObjectId;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class RecipeTests {

    @InjectMock
    RecipeService service;

    @TestHTTPEndpoint(RecipeRessource.class)
    @TestHTTPResource
    URL url;

    @Test
    void searchTest() {

        var recipes = new ArrayList<Recipe>();
        recipes.add(new Recipe("Blaukraut", "Glück gehabt. Das gibt es wirklich"));

        Mockito.when(service.list()).thenReturn(recipes);

        RestAssured.given()
                .when().get(url)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("size()", is(recipes.size()));

        Mockito.verify(service, Mockito.times(1)).list();
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    void postRecipeTest() throws URISyntaxException {

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


    private static class UriMatcher extends TypeSafeMatcher<URI> {

        private final URI expected;

        public UriMatcher(URI expected) {
            this.expected = expected;
        }

        public static UriMatcher matchesLocation(URL baseUri, ObjectId id) throws URISyntaxException {
            var expected = UriBuilder
                    .fromUri(baseUri.toURI())
                    .path(id.toString())
                    .build();

            return new UriMatcher(expected);
        }

        @Override
        protected boolean matchesSafely(URI item) {
            return item.equals(expected);
        }

        @Override
        public void describeTo(Description description) {
            description.appendValue(expected);
        }
    }
}
