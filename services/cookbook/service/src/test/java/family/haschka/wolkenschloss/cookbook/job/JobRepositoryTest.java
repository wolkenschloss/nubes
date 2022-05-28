package family.haschka.wolkenschloss.cookbook.job;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.groups.UniAwait;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.net.URI;
import java.util.UUID;

@QuarkusTest
public class JobRepositoryTest {

    @Inject
    ImportJobRepository repository;

    @Test
    public void insertJob() {
        var id = UUID.randomUUID();
        var job = ImportJob.create(null, URI.create("/myRecipe/123"));
        job = job.located(URI.create("/myLocation/123"));

        Uni<ImportJob> persist = repository.persist(job);
        UniAwait<ImportJob> await = persist.await();
        await.indefinitely();


        Assertions.assertEquals(1, repository.findAll().count().await().indefinitely());

        var clone = repository.findById(job.jobId).await().indefinitely();
        Assertions.assertNotNull(clone);
        Assertions.assertEquals(job, clone);
    }

    @Test
    public void checkRepository() {
        Assertions.assertNotNull(repository);
    }
}
