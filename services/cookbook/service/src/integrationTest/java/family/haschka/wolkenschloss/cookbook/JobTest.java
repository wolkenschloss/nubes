package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.testing.MockServer;
import family.haschka.wolkenschloss.cookbook.testing.RecipeWebsite;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

import javax.json.Json;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusIntegrationTest
@MockServer()
public class JobTest {

    @BeforeAll
    public static void serve(RecipeWebsite website) {
        website.serve();
    }

    @TestFactory
    @DisplayName("POST /job")
    public Stream<DynamicTest> postJobTest() {
        return Stream.of(
                        new PostJobTestcase(
                                "valid job",
                                "lasagne.html",
                                PostJobTestcase::success),
                        new PostJobTestcase(
                                "missing resource",
                                "chili.html",
                                PostJobTestcase.error("The data source cannot be read")),
                        new PostJobTestcase(
                                "not a recipe",
                                "news.html",
                                PostJobTestcase.error("The data source does not contain an importable recipe")),
                        new PostJobTestcase(
                                "more than one recipe",
                                "menu.html",
                                PostJobTestcase.error("Data source contains more than one recipe"))
                )

                .map(testcase -> DynamicTest.dynamicTest(testcase.display, () -> {
                    var template = UriBuilder.fromUri("http://localhost:8080/{resource}");
                    var job = template
                            .host("localhost")
                            .port(Integer.parseInt(System.getProperty("quarkus.http.port")))
                            .build("job");

                    var location = RestAssured
                            .given()
                            .body(testcase.job())
                            .contentType(ContentType.JSON)
                            .when()
                            .post(job)
                            .then()
                            .statusCode(Response.Status.CREATED.getStatusCode())
                            .header("Location", r -> equalTo(UriBuilder.fromUri(job)
                                    .path("{jobId}")
                                    .build(r.path("jobId").toString()).toString()))
                            .extract().header("location");

                    await().atMost(Duration.ofSeconds(5))
                            .pollInterval(1, TimeUnit.SECONDS)
                            .pollDelay(1, TimeUnit.SECONDS)
                            .untilAsserted(() -> testcase.assertion.accept(
                                    RestAssured.given()
                                            .accept(ContentType.JSON)
                                            .when()
                                            .get(location)
                                            .then()
                                            .statusCode(Response.Status.OK.getStatusCode())));
                }));
    }

    public record PostJobTestcase(
            String display,
            String filename,
            ThrowingConsumer<ValidatableResponse> assertion) {

        // z.B. /recipe/61aa3fe8537e6c3885e6816a
        private static final Pattern locationPattern = Pattern.compile(
                "/recipe/[a-f0-9]{24}");

        static private void success(ValidatableResponse assertion) {
            assertion
                    .body("state", equalTo("INCOMPLETE"))
                    .body("location", matchesPattern(locationPattern))
                    .body("error", equalTo(null));
        }

        @NotNull
        private static ThrowingConsumer<ValidatableResponse> error(String message) {
            return assertion -> assertion
                    .body("state", equalTo("FAILED"))
                    .body("error", equalTo(message));
        }

        private String job() {

            var factory = Json.createBuilderFactory(new HashMap<>());
            var builder = factory.createObjectBuilder();

            URI orderUri = RecipeWebsite.orderUriFrom("__files/{filename}").build(filename);

            builder.add("order", orderUri.toString());
            return builder.build().toString();
        }
    }
}
