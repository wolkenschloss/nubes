package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.job.*;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class JobServiceTest {

    @InjectMock
    ImportJobRepository repository;

    @SuppressWarnings("unused")
    @InjectMock
    RecipeService recipeService;

    @Inject
    JobService sut;

    @Inject
    JobReceivedObserver observer;

    @Test
    public void addJobTest() throws ExecutionException, InterruptedException {

        var id = UUID.randomUUID();

        // Mongo DB setzt die ID, wenn die Entität persistiert wird.
        Mockito.doAnswer(x -> null).when(repository).persist(any(ImportJob.class));

        var job = new ImportJob();
        job.order = "https://meinerezepte.local/lasagen";
        job.jobId = id;

        var future = sut.addJob(job);

        future.thenAccept(event -> {

            Assertions.assertTrue(observer.events.contains(event));
            var expected = new JobReceivedEvent(event.jobId(), URI.create(job.order));

            Assertions.assertEquals(expected, event);

        }).toCompletableFuture().get();

        Mockito.verify(repository, Mockito.times(1)).persist(job);
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Inject
    Event<JobCompletedEvent> completed;

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
        jobAfterCompletion.state =State.COMPLETED;
        jobAfterCompletion.location = event.location();

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobBeforeCompletion));
        Mockito.doAnswer(x -> null).when(repository).update(jobAfterCompletion);

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobAfterCompletion));

        completed.fire(event);

        var j = sut.get(event.jobId());
        var expected = new ImportJob();
        expected.jobId = id;
        expected.state = State.COMPLETED;
        expected.location = event.location();
        expected.error = null;

        Assertions.assertEquals(expected, j.orElseThrow(AssertionFailedError::new));

        Mockito.verify(repository, Mockito.times(2)).findByIdOptional(any());
        Mockito.verify(repository, Mockito.times(1)).update(any(ImportJob.class));
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

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobBeforeCompletion));
        Mockito.doAnswer(x -> null).when(repository).update(jobAfterCompletion);

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobAfterCompletion));

        completed.fire(event);

        var j = sut.get(event.jobId());
        var expected = new ImportJob();
        expected.jobId = id;
        expected.state = State.COMPLETED;
        expected.error = event.error();
        expected.location = event.location();

        Assertions.assertEquals(expected, j.orElseThrow(AssertionFailedError::new));
        Assertions.assertEquals(expected.error, j.orElseThrow(AssertionFailedError::new).error);

        Mockito.verify(repository, Mockito.times(2)).findByIdOptional(any());
        Mockito.verify(repository, Mockito.times(1)).update(jobAfterCompletion);
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Singleton
    static class JobReceivedObserver {

        private List<JobReceivedEvent> events;

        @PostConstruct
        void init() {
            events = new CopyOnWriteArrayList<>();
        }

        void observeAsync(@ObservesAsync JobReceivedEvent event) {
            events.add(event);
        }

        // Hm
        List<JobReceivedEvent> getEvents() {
            return events;
        }
    }
}
