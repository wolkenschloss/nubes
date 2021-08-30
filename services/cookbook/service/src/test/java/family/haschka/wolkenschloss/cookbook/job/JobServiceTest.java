package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.RecipeService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@QuarkusTest
public class JobServiceTest {

    @InjectMock
    ImportJobRepository repository;

    @SuppressWarnings("unused")
    @InjectMock
    RecipeService recipeService;

    @Inject
    JobService sut;

    @Test
    public void addJobTest() {

        var job = new ImportJob();
        job.order = "https://meinerezepte.local/lasagen";
        job.jobId = UUID.randomUUID();

        Mockito.when(repository.persist(job))
                .thenReturn(Uni.createFrom().item(job));

        var expected = new JobReceivedEvent(job.jobId, URI.create(job.order));
        var event = sut.addJob(job).log("test");
        var subscriber = event.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertItem(expected);

        Mockito.verify(repository, Mockito.times(1)).persist(job);
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Inject
    EventBus bus;

    @Test
    public void jobCompletedSuccessfullyTest() {
        var id = UUID.randomUUID();
        var event = new JobCompletedEvent(id, URI.create("/recipe/123"), null);

        var jobBeforeCompletion = new ImportJob();
        jobBeforeCompletion.jobId = id;
        jobBeforeCompletion.state = State.IN_PROGRESS;
        jobBeforeCompletion.location = null;

        var jobAfterCompletion = new ImportJob();
        jobAfterCompletion.jobId = id;
        jobAfterCompletion.state = State.COMPLETED;
        jobAfterCompletion.location = event.location();

        Mockito.when(repository.findByIdOptional(id))
                .thenReturn(Uni.createFrom().item(Optional.of(jobBeforeCompletion)));

        Mockito.when(repository.update(jobAfterCompletion))
                .thenReturn(Uni.createFrom().item(jobAfterCompletion));

        // when!
        bus.publish("job.completed", event);

        var expected = new ImportJob();
        expected.jobId = id;
        expected.state = State.COMPLETED;
        expected.location = event.location();
        expected.error = null;

        Mockito.verify(repository, Mockito.timeout(1000).times(1)).findByIdOptional(id);
        Mockito.verify(repository, Mockito.timeout(1000).times(1)).update(expected);
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    public void jobCompletedWithErrorsTest() {
        var id = UUID.randomUUID();
        var event = new JobCompletedEvent(
                id,
                URI.create("https://meinerezepte.local.lasagne.html"),
                "The data source cannot be read");

        var jobBeforeCompletion = new ImportJob();
        jobBeforeCompletion.jobId = id;
        jobBeforeCompletion.state = State.IN_PROGRESS;
        jobBeforeCompletion.error = null;

        var jobAfterCompletion = new ImportJob();
        jobAfterCompletion.jobId = id;
        jobAfterCompletion.state = State.COMPLETED;
        jobAfterCompletion.error = event.error();

        Mockito.when(repository.findByIdOptional(id))
                .thenReturn(Uni.createFrom().item(Optional.of(jobBeforeCompletion)));

        Mockito.when(repository.update(jobAfterCompletion))
                        .thenReturn(Uni.createFrom().item(jobAfterCompletion));

        Mockito.when(repository.findByIdOptional(id))
                .thenReturn(Uni.createFrom().item(Optional.of(jobAfterCompletion)));

        bus.publish("job.completed", event);

        var expected = new ImportJob();
        expected.jobId = id;
        expected.state = State.COMPLETED;
        expected.error = event.error();
        expected.location = event.location();


        Mockito.verify(repository, Mockito.timeout(1000).times(1)).findByIdOptional(id);
        Mockito.verify(repository, Mockito.timeout(1000).times(1)).update(jobAfterCompletion);
        Mockito.verifyNoMoreInteractions(repository);
    }
}
