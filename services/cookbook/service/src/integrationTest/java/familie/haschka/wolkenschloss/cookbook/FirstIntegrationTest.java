package familie.haschka.wolkenschloss.cookbook;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import org.apache.http.HttpStatus;
import org.bson.types.ObjectId;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;


@QuarkusIntegrationTest
@QuarkusTestResource(value = MongoDbResource.class, restrictToAnnotatedClass = true)
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

    @Test
    public void createRecipe() {
        var port = System.getProperty("quarkus.http.port");

        var recipe = "{\"title\": \"Schlammkrabbeneintopf\", \"herstellung\": \"Bekannt.\"}";

        var url = "http://localhost:" + port + "/recipe";

        var location = RestAssured
                .given()
                    .body(recipe)
                    .contentType(MediaType.APPLICATION_JSON)
//                .log().all()
                .when()
                    .post(url)
                .then()
//                .log().all()
                .   statusCode(HttpStatus.SC_CREATED)
                .extract()
                    .header("Location");

        logger.warn("Location: {}", location);

        // TODO:
        // 1. Aus location die ID ermitteln
        // 2. GET /recipe liefert alle Recipes. Darin sollte ID enthalten sein.
        // 3. GET /recipe/ID liefert das gespeicherte Recipe zurück.
        RestAssured
                .given()
//                    .log().all()
                .when()
                    .get(url)
                .then()
//                    .log().all()
                    .statusCode(HttpStatus.SC_OK);
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
