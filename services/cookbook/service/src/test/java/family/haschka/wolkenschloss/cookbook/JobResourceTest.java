package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.job.IJobService;
import family.haschka.wolkenschloss.cookbook.job.ImportJob;
import family.haschka.wolkenschloss.cookbook.job.JobResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.mapper.ObjectMapperType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class JobResourceTest {

    public static final String JOB_URL = "http://meinkochbuch.local/lasagne.html";
    @InjectMock
    IJobService service;

    @TestHTTPEndpoint(JobResource.class)
    @TestHTTPResource
    URL url;

    @Test
    public void createImportJobTest() {

        var id = UUID.randomUUID();

        Mockito.doAnswer(x -> {
            x.getArgument(0, ImportJob.class).setJobId(id);
            return null;
        }).when(service).addJob(any(ImportJob.class));

        ImportJob job = new ImportJob();
        job.setUrl(JOB_URL);

        RestAssured.given()
                .body(job, ObjectMapperType.JSONB).contentType(MediaType.APPLICATION_JSON)
                .when()
                .post(url)
                .then()
                .statusCode(HttpStatus.SC_CREATED)
                .header("Location", response -> {
                    var expected = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();
                    return equalTo(expected.toString());
                });

        Mockito.verify(service, Mockito.times(1)).addJob(Mockito.any(ImportJob.class));
        Mockito.verifyNoMoreInteractions(service);
    }

    @Test
    public void readImportJobNotFoundTest() throws URISyntaxException {
        var id = UUID.randomUUID();
        var location = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();

        Mockito.when(service.get(id)).thenReturn(Optional.empty());

        RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .log().all()
                .when()
                .get(location)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    public void readImportJobFoundTest() throws URISyntaxException {
        var id = UUID.randomUUID();
        var location = UriBuilder.fromUri(url.toURI()).path(id.toString()).build();

        var job = new ImportJob();
        job.setJobId(id);
        job.setUrl(JOB_URL);

        Mockito.when(service.get(id)).thenReturn(Optional.of(job));

        var actual = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .log().all()
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
}
