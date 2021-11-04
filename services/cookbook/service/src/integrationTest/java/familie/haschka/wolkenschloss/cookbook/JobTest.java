package familie.haschka.wolkenschloss.cookbook;

import familie.haschka.wolkenschloss.cookbook.testing.WiremockRecipes;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

import javax.json.Json;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusIntegrationTest
@QuarkusTestResource(value = WiremockRecipes.class)
public class JobTest {

    @Test
    @DisplayName("import recipe from website")
    public void importRecipe() {
        var mock_url = System.getProperty("family.haschka.wiremock.recipes");
        var job_url = String.format("%s/lasagne.html", mock_url);
        var port = System.getProperty("quarkus.http.port");
        var url = String.format("http://localhost:%s/job", port);

        var response = RestAssured
                .given()
                .body(createJob(job_url))
                .contentType(ContentType.JSON)
                .when()
                .post(url)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", r -> equalTo(url + "/" + r.path("jobId")));

        String jobId = response.extract().path("jobId");

        // Wiremock delays response by 3 seconds
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(1, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .until(() -> RestAssured.given()
                        .accept(ContentType.JSON)
                        .when()
                        .get(url + "/" + jobId)
                        .then()
                        .statusCode(Response.Status.OK.getStatusCode())
                        .extract()
                        .path("state")
                        .equals("INCOMPLETE"));
    }

    private String createJob(String job_url) {

        Map<String, ?> config = new HashMap<>();
        var factory = Json.createBuilderFactory(config);
        var builder = factory.createObjectBuilder();

        builder.add("order",  job_url);
        return builder.build().toString();
    }

    public record PostJobTestcase(String name, String requestedData,
                                  ThrowingConsumer<ValidatableResponse> assertion) {
        public PostJobTestcase(
                String name,
                String requestedData,
                @SuppressWarnings("CdiInjectionPointsInspection") ThrowingConsumer<ValidatableResponse> assertion) {
            this.name = name;
            this.requestedData = requestedData;
            this.assertion = assertion;
        }

        public String jobBody() {
            return String.format("%s/%s", getMockUrl(), requestedData);
        }

        private String getMockUrl() {
            return System.getProperty("family.haschka.wiremock.recipes");
        }
    }

    @TestFactory
    @DisplayName("POST /job")
    public Stream<DynamicTest> postJobTest() {
        return Stream.of(
                        new PostJobTestcase("valid job", "lasagne.html",
                                response -> response
                                        .body("state", equalTo("INCOMPLETE"))
                                        .body("location", matchesPattern("/recipe/[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}"))
                                        .body("error", equalTo(null))),

                        new PostJobTestcase("missing resource", "chili.html",
                                response -> response
                                        .body("state", equalTo("FAILED"))
                                        .body("error", equalTo("The data source cannot be read"))),


                        new PostJobTestcase("not a recipe", "news.html",
                                response -> response
                                        .body("state", equalTo("FAILED"))
                                        .body("error", equalTo("The data source does not contain an importable recipe"))),

                        new PostJobTestcase("more than one recipe", "menu.html",
                                response -> response
                                        .body("state", equalTo("FAILED"))
                                        .body("error", equalTo("Data source contains more than one recipe")))
                )

                .map(testcase -> DynamicTest.dynamicTest(testcase.name, () -> {
                    var port = System.getProperty("quarkus.http.port");
                    var url = String.format("http://localhost:%s/job", port);

                    var location = RestAssured
                            .given()
                            .body(createJob(testcase.jobBody()))
                            .contentType(ContentType.JSON)
                            .when()
                            .post(url)
                            .then()
                            .statusCode(Response.Status.CREATED.getStatusCode())
                            .header("Location", r -> equalTo(url + "/" + r.path("jobId")))
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
}
