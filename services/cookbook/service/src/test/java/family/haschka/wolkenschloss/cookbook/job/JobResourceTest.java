package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.testing.Blueprint;
import io.quarkus.test.common.http.TestHTTPEndpoint;
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
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;

@Blueprint(tags = {"ReST Resource", "Unit test"})
@QuarkusTest
@TestHTTPEndpoint(JobResource.class)
@DisplayName("Job Resource")
public class JobResourceTest {

    public static final URI JOB_URL = URI.create("http://meinkochbuch.local/lasagne.html");

    @InjectMock
    JobService service;

    @Inject
    Jsonb jsonb;

    @Test
    @DisplayName("Verify RestAssured baseUri")
    public void verifyBaseUri() throws URISyntaxException {

        var expectedUri = UriBuilder.fromUri(RestAssured.baseURI)
                .port(RestAssured.port)
                .path(RestAssured.basePath)
                .build();

        Assertions.assertEquals(new URI("http://localhost:9292/cookbook/job"), expectedUri);
    }

    @Test
    @DisplayName("POST /job")
    public void createImportJobTest() {

        UUID id = UUID.randomUUID();
        ImportJob order = new ImportJob(null, URI.create("http://meinerezepte.local/lasagne.html"), null, null, null);
        ImportJob result = ImportJob.create(id, order.order());

        Mockito.when(service.create(any(URI.class)))
                .thenReturn(Uni.createFrom().item(result));

        RestAssured.given()
                .body(order, ObjectMapperType.JSONB)
                .contentType(MediaType.APPLICATION_JSON)
                .when()
                .post()
                .then()
                .statusCode(Response.Status.CREATED.getStatusCode())
                .header("Location", response -> equalTo(UriBuilder.fromUri(RestAssured.baseURI)
                        .port(RestAssured.port)
                        .path(RestAssured.basePath)
                        .path(id.toString())
                        .build()
                        .toString()));

        Mockito.verify(service, Mockito.times(1)).create(order.order());
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("GET /job/{id} not found")
    public void readImportJobNotFoundTest() {
        var id = UUID.randomUUID();

        Mockito.when(service.get(id))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(id.toString())
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("GET /job/{id} ok")
    public void readImportJobFoundTest() {
        var id = UUID.randomUUID();

        var job = new ImportJob(id, JOB_URL, null, null, null);

        Mockito.when(service.get(id))
                .thenReturn(Uni.createFrom().item(Optional.of(job)));

        var actual = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .when()
                .get(id.toString())
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .as(ImportJobAnnotation.class);

        Assertions.assertEquals(job, new ImportJobAdapter().adaptFromJson(actual));

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
        return new ImportJob(UUID.randomUUID(), JOB_URL, State.COMPLETED, URI.create("https://google.de"), null);
    }
}
