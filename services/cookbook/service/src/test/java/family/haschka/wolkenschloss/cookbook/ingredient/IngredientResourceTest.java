package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.hamcrest.core.Is.is;

@QuarkusTest
@DisplayName("Ingredient Resource")
@TestHTTPEndpoint(IngredientRessource.class)
public class IngredientResourceTest {

    @InjectMock
    IngredientService service;

    enum GetIngredientTestCase {
        PARAMETER_FROM("?from=42", uri -> uri.queryParam("from", 42), 42, -1, null),
        PARAMETER_TO("?to=5", uri -> uri.queryParam("to", 5), 0, 5, null),
        PARAMETER_SEARCH("?q=something", uri -> uri.queryParam("q", "something"), 0, -1, "something"),
        NO_PARAMETERS("(no parameters)", uri -> uri, 0, -1, null),
        ALL_PARAMETERS("?from=13&to=26&q=something",
                uri -> uri.queryParam("from", 13)
                        .queryParam("to", 26)
                        .queryParam("q", "something"), 13, 26, "something");

        GetIngredientTestCase(String display, Function<UriBuilder, UriBuilder> uri, int from, int to, String search) {
            this.display = display;
            this.uri = uri;
            this.from = from;
            this.to = to;
            this.search = search;
        }

        private final String display;
        private final Function<UriBuilder, UriBuilder> uri;
        private final int from;
        private final int to;
        private final String search;

        public URL url() throws MalformedURLException {
            var uriBuilder = UriBuilder.fromUri(RestAssured.baseURI).port(RestAssured.port).path(RestAssured.basePath);
            return uri.apply(uriBuilder).build().toURL();
        }

        public int count() {
            return (to == -1 ? 99 : to) - from;
        }

        @Override
        public String toString() {
            return this.display;
        }
    }

    @ParameterizedTest
    @EnumSource(GetIngredientTestCase.class)
    @DisplayName("GET /ingredient")
    void getIngredients(GetIngredientTestCase testcase) throws MalformedURLException {

        var ingredients = IntStream.rangeClosed(testcase.from, testcase.to)
                .mapToObj(i -> String.format("Ingredient #%d", i))
                .map(title -> new Ingredient(UUID.randomUUID(), title))
                .toList();

        var toc = new TableOfContents(testcase.count(), ingredients);

        Mockito.when(service.list(testcase.from, testcase.to, testcase.search))
                .thenReturn(Uni.createFrom().item(toc));

        RestAssured.given()
                .when()
                .accept(MediaType.APPLICATION_JSON)
                .get(testcase.url())
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body("content.size()", is(ingredients.size()));

        Mockito.verify(service, Mockito.times(1))
                .list(testcase.from, testcase.to, testcase.search);

        Mockito.verifyNoMoreInteractions(service);
    }
}
