package familie.haschka.wolkenschloss.cookbook;

import familie.haschka.wolkenschloss.cookbook.testing.MockServerResource;
import familie.haschka.wolkenschloss.cookbook.testing.WiremockRecipes;
import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import javax.json.Json;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;

@QuarkusIntegrationTest
@QuarkusTestResource(MockServerResource.class)
public class JobTest {

//    @BeforeEach
    public void initMock() throws URISyntaxException, IOException {
        var host = System.getProperty(MockServerResource.TESTCLIENT_HOST_CONFIG);
        var port = Integer.parseInt(System.getProperty(MockServerResource.TESTCLIENT_PORT_CONFIG));

        var client = new MockServerClient(host, port);
        client.reset();

        var uri = this.getClass().getClassLoader().getResource("__files/lasagne.html").toURI();
        try(InputStream in = uri.toURL().openStream()) {
            byte[] bytes = in.readAllBytes();
            var body = new String(bytes, Charset.defaultCharset());

            client.when(HttpRequest.request()
                            .withMethod("GET")
                            .withPath(".*"))
                    .respond(HttpResponse.response()
                            .withDelay(TimeUnit.MILLISECONDS, 500)
                            .withStatusCode(Response.Status.OK.getStatusCode())
                            .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                            .withBody(body));
        }
    }

    @Test
    @DisplayName("import recipe from website using mock server")
    public void importRecipeMockServer() {

        var testcase = new PostJobTestcase(
                "equal",
                "lasagne.html",
                PostJobTestcase.defaultBehaviour("lasagne.html"),
                assertion -> {});

        testcase.initMock();
        var applicationTemplate = System.getProperty(MockServerResource.APPLICATION_TEMPLATE_CONFIG);
        var mock_url = String.format("%s/lasagne.html", applicationTemplate);

        var port = System.getProperty("quarkus.http.port");
        var jobUrl = String.format("http://localhost:%s/job", port);

        var response = RestAssured
                .given()
                .log().all()
                .body(createJob(mock_url))
                .contentType(ContentType.JSON)
                .when()
                .post(jobUrl)
                .then()
                .log().all()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", r -> equalTo(jobUrl + "/" + r.path("jobId")));

        String jobId = response.extract().path("jobId");

        // Wiremock delays response by 3 seconds
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(1, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .until(() -> RestAssured.given()
                        .accept(ContentType.JSON)
                        .when()
                        .get(jobUrl + "/" + jobId)
                        .then()
                        .log().all()
                        .statusCode(Response.Status.OK.getStatusCode())
                        .extract()
                        .path("state")
                        .equals("INCOMPLETE"));
    }

    private String createJob(String job_url) {

        Map<String, ?> config = new HashMap<>();
        var factory = Json.createBuilderFactory(config);
        var builder = factory.createObjectBuilder();

        builder.add("order", job_url);
        return builder.build().toString();
    }

    public record PostJobTestcase(
            String name,
            String requestedData,
            BiConsumer<HttpRequest, HttpResponse> when,
            ThrowingConsumer<ValidatableResponse> assertion) {
        public PostJobTestcase(
                String name,
                String requestedData,
                @SuppressWarnings("CdiInjectionPointsInspection") BiConsumer<HttpRequest, HttpResponse> when,
                @SuppressWarnings("CdiInjectionPointsInspection") ThrowingConsumer<ValidatableResponse> assertion) {
            this.name = name;
            this.requestedData = requestedData;
            this.when = when;
            this.assertion = assertion;
        }

        public void initMock() {
                var host = System.getProperty(MockServerResource.TESTCLIENT_HOST_CONFIG);
                var port = Integer.parseInt(System.getProperty(MockServerResource.TESTCLIENT_PORT_CONFIG));

                var client = new MockServerClient(host, port);
                client.reset();

                var request = HttpRequest.request();
                var response = HttpResponse.response();

                when.accept(request, response);
                client.when(request).respond(response);
        }

        public static BiConsumer<HttpRequest, HttpResponse> defaultBehaviour(String filename) {
            return (request, response) -> {
                var path = String.format("__files/%s", filename);
                URI uri = null;
                try {
                    uri = JobTest.class.getClassLoader().getResource(path).toURI();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                try(InputStream in = uri.toURL().openStream()) {
                    byte[] bytes = in.readAllBytes();
                    var body = new String(bytes, Charset.defaultCharset());

                    request.withMethod("GET").withPath(".*");
                            response
//                                    .withDelay(TimeUnit.MILLISECONDS, 500)
                                    .withStatusCode(Response.Status.OK.getStatusCode())
                                    .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                    .withBody(body);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            };
        }
    }

    @TestFactory
    @DisplayName("POST /job")
    public Stream<DynamicTest> postJobTest() {
        return Stream.of(
                        new PostJobTestcase("valid job", "lasagne.html",
                                PostJobTestcase.defaultBehaviour("lasagne.html"),
                                assertion -> assertion
                                        .body("state", equalTo("INCOMPLETE"))
                                        .body("location", matchesPattern("/recipe/[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89aAbB][a-f0-9]{3}-[a-f0-9]{12}"))
                                        .body("error", equalTo(null))),
                        new PostJobTestcase("missing resource", "chili.html",
                                (request, response) -> {
                                    request.withMethod("GET").withPath(".*");
                                    response.withStatusCode(Response.Status.NOT_FOUND.getStatusCode());
                                },
                                assertion -> assertion
                                        .body("state", equalTo("FAILED"))
                                        .body("error", equalTo("The data source cannot be read"))),
                        new PostJobTestcase("not a recipe", "news.html",
                                PostJobTestcase.defaultBehaviour("news.html"),
                                assertion -> assertion
                                        .body("state", equalTo("FAILED"))
                                        .body("error", equalTo("The data source does not contain an importable recipe"))),
                        new PostJobTestcase("more than one recipe", "menu.html",
                                PostJobTestcase.defaultBehaviour("menu.html"),
                                assertion -> assertion
                                        .body("state", equalTo("FAILED"))
                                        .body("error", equalTo("Data source contains more than one recipe")))
                )

                .map(testcase -> DynamicTest.dynamicTest(testcase.name, () -> {
                    testcase.initMock();
                    var port = System.getProperty("quarkus.http.port");
                    var url = String.format("http://localhost:%s/job", port);

                    var applicationTemplate = System.getProperty(MockServerResource.APPLICATION_TEMPLATE_CONFIG);
                    var mock_url = String.format("%s/%s", applicationTemplate, testcase.requestedData);

                    var location = RestAssured
                            .given()
                            .body(createJob(mock_url))
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
