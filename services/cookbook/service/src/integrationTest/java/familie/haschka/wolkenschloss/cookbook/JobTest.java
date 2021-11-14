package familie.haschka.wolkenschloss.cookbook;

import familie.haschka.wolkenschloss.cookbook.testing.FileResource;
import familie.haschka.wolkenschloss.cookbook.testing.MockServerClientExtension;
import familie.haschka.wolkenschloss.cookbook.testing.MockServerClientParameterResolver;
import familie.haschka.wolkenschloss.cookbook.testing.MockServerResource;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import javax.json.Json;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusIntegrationTest
@MockServerClientExtension
public class JobTest {

    private static final Logger logger = Logger.getLogger(JobTest.class);

    @BeforeAll
    public static void initMockServer(TestInfo info, MockServerClient client) {
        logger.infov("Testing {0}", info.getDisplayName());

        client.reset()
                .when(HttpRequest.request().withMethod("GET"))
                .respond((HttpRequest request) -> {
                    logger.infov("responding {0} {1}", request.getMethod(), request.getPath());
                    var path = request.getPath();
                    var file = new FileResource(path.getValue().replaceAll("^/", ""));
                    try {
                        return HttpResponse.response()
                                .withBody(file.read())
                                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withStatusCode(Response.Status.OK.getStatusCode());
                    } catch (Exception e) {
                        logger.errorv(e, "Error responding to {0} {1}",
                                request.getMethod(),
                                request.getPath());

                        return HttpResponse.response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode());
                    }
                });
    }

    @TestFactory
    @DisplayName("POST /job")
    public Stream<DynamicTest> postJobTest() {
        return Stream.of(
                        new PostJobTestcase(
                                "valid job",
                                "__files/lasagne.html",
                                PostJobTestcase::success),
                        new PostJobTestcase(
                                "missing resource",
                                "__files/chili.html",
                                PostJobTestcase.error("The data source cannot be read")),
                        new PostJobTestcase(
                                "not a recipe",
                                "__files/news.html",
                                PostJobTestcase.error("The data source does not contain an importable recipe")),
                        new PostJobTestcase(
                                "more than one recipe",
                                "__files/menu.html",
                                PostJobTestcase.error("Data source contains more than one recipe"))
                )

                .map(testcase -> DynamicTest.dynamicTest(testcase.name, () -> {
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
            String name,
            String requestedData,
            ThrowingConsumer<ValidatableResponse> assertion) {

        private static final Pattern locationPattern = Pattern.compile(
                "/recipe/[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}");

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

            var applicationTemplate = System.getProperty(MockServerResource.APPLICATION_TEMPLATE_CONFIG);
            var mock_url = String.format("%s/%s", applicationTemplate, requestedData);

            builder.add("order", mock_url);
            return builder.build().toString();
        }
    }
}
