package family.haschka.wolkenschloss.cookbook.job;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.net.URI;
import java.util.UUID;

@QuarkusTest
@DisplayName("Job Repository")
public class JobRepositoryTest {

    @Inject
    ImportJobRepository repository;

    ImportJob theJob;

    @BeforeEach
    public void persistJob() {
        theJob = ImportJob.create(UUID.randomUUID(), URI.create("/myRecipe/123"))
                .complete(URI.create("/myLocation/123"), "Das hat nicht geklappt.");

        repository.deleteAll().await().indefinitely();
        repository.persist(theJob).await().indefinitely();
    }

    @Test
    @DisplayName("should find job by id")
    public void findJob() {
        var clone = repository.findById(theJob.jobId()).await().indefinitely();
        Assertions.assertNotNull(clone);
        Assertions.assertEquals(theJob, clone);
    }

    @Test
    @DisplayName("should delete job by id")
    public void deleteJob() {
        repository.deleteById(theJob.jobId()).await().indefinitely();
        Assertions.assertEquals(0, repository.findAll().count().await().indefinitely());
    }

    @Test
    @DisplayName("should update job")
    public void updateJob() {

        repository.findById(theJob.jobId())
                .map(job -> job.complete(URI.create("/myOtherLocation"), null))
                .flatMap(job -> repository.update(job))
                .await().indefinitely();

        Assertions.assertNotEquals(theJob, repository.findById(theJob.jobId()).await().indefinitely());
    }

    @Test
    @DisplayName("should exist")
    public void checkRepository() {
        Assertions.assertNotNull(repository);
    }
}
