package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.IdentityGenerator;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.mapper.ObjectMapperType;
import io.smallrye.mutiny.Uni;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class JobResourceTest {

    public static final URI JOB_URL = URI.create("http://meinkochbuch.local/lasagne.html");

    @InjectMock
    JobService service;

    @TestHTTPEndpoint(JobResource.class)
    @TestHTTPResource
    URL url;

    @Inject
    Jsonb jsonb;

    @InjectMock
    IdentityGenerator identityGenerator;

    @Test
    @DisplayName("POST /job")
    public void createImportJobTest() {

        UUID id = UUID.randomUUID();
        ImportJob order = new ImportJob();
        order.order = URI.create("http://meinerezepte.local/lasagne.html");
        ImportJob result = ImportJob.create(id, order.order);

        Mockito.when(service.create(any(URI.class)))
                .thenReturn(Uni.createFrom().item(result));

        RestAssured.given()
                .body(order, ObjectMapperType.JSONB)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post(url)
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", response -> {
                    var expected = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();
                    return equalTo(expected.toString());
                });

        Mockito.verify(service, Mockito.times(1)).create(order.order);
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /job/{id} not found")
    public void readImportJobNotFoundTest() throws URISyntaxException {
        var id = UUID.randomUUID();
        var location = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();

        Mockito.when(service.get(id))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("GET /job/{id} ok")
    public void readImportJobFoundTest() throws URISyntaxException {
        var id = UUID.randomUUID();
        var location = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();

        var job = new ImportJob();
        job.jobId = id;
        job.order = JOB_URL;

        Mockito.when(service.get(id))
                .thenReturn(Uni.createFrom().item(Optional.of(job)));

        var actual = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(location)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(ImportJob.class);

        Assertions.assertEquals(job, actual);

        Mockito.verify(service, Mockito.times(1)).get(id);
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    public void ImportJobSerializationTest() {

        ImportJob job = getImportJob();
        String serialized = jsonb.toJson(job);
        ImportJob clone  = jsonb.fromJson(serialized, ImportJob.class);

        Assertions.assertEquals(job, clone);
    }

    private ImportJob getImportJob() {
        ImportJob job = new ImportJob();
        job.error = null;
        job.jobId = UUID.randomUUID();
        job.location = URI.create("https://google.de");
        job.state = State.COMPLETED;
        job.order = JOB_URL;

        return job;
    }
}
