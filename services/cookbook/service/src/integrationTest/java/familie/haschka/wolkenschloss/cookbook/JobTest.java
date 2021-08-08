package familie.haschka.wolkenschloss.cookbook;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

@QuarkusIntegrationTest
@QuarkusTestResource(value = MongoDbResource.class, restrictToAnnotatedClass = true)
@QuarkusTestResource(WiremockRecipes.class)
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
                .log().all()
                .when()
                .post(url)
                .then()
                .log().all()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", r -> equalTo(url + "/" + r.path("jobId")));

        String jobId = response.extract().path("jobId");

        // Wiremock delays response by 3 seconds
        await().atMost(Duration.ofSeconds(5))
                .pollInterval(1, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .until(() -> RestAssured.given()
                        .log().all()
                        .accept(ContentType.JSON)
                        .when()
                        .get(url + "/" + jobId)
                        .then()
                        .log().all()
                        .statusCode(HttpStatus.SC_OK)
                        .extract()
                        .path("state")
                        .equals("COMPLETED"));
    }

    private String createJob(String job_url) {

        Map<String, ?> config = new HashMap<>();
        var factory = Json.createBuilderFactory(config);
        var builder = factory.createObjectBuilder();

        builder.add("url", job_url);
        return builder.build().toString();
    }
}
