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
        Mockito.doAnswer(x -> {
            x.getArgument(0, ImportJob.class).setJobId(id);
            return null;
        }).when(repository).persist(any(ImportJob.class));

        var job = new ImportJob();
        job.setUrl("https://meinerezepte.local/lasagen");
        job.setJobId(id);

        var future = sut.addJob(job);

        future.thenAccept(event -> {

            Assertions.assertTrue(observer.events.contains(event));
//            Assertions.assertEquals(1, observer.getEvents().size());
            var expected = new JobReceivedEvent();
            expected.jobId = event.jobId;
            expected.source = URI.create(job.getUrl());

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
        var event = new JobCompletedEvent();
        event.jobId = id;
        event.location = Optional.of(URI.create("/recipe/123"));
        event.error = Optional.empty();

        var jobBeforeCompletion = new ImportJob();
        jobBeforeCompletion.setJobId(id);
        jobBeforeCompletion.setState(ImportJob.State.IN_PROGRESS);
        jobBeforeCompletion.setLocation(null);

        var jobAfterCompletion = new ImportJob();
        jobAfterCompletion.setJobId(id);
        jobAfterCompletion.setState(ImportJob.State.COMPLETED);
        jobAfterCompletion.setLocation(event.location.orElse(null));

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobBeforeCompletion));
        Mockito.doAnswer(x -> null).when(repository).update(jobAfterCompletion);

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobAfterCompletion));

        completed.fire(event);

        var j = sut.get(event.jobId);
        var expected = new ImportJob();
        expected.setJobId(id);
        expected.setState(ImportJob.State.COMPLETED);
        expected.setLocation(event.location.orElse(null));
        expected.setError(null);

        Assertions.assertEquals(expected, j.orElseThrow(AssertionFailedError::new));

        Mockito.verify(repository, Mockito.times(2)).findByIdOptional(any());
        Mockito.verify(repository, Mockito.times(1)).update(any(ImportJob.class));
        Mockito.verifyNoMoreInteractions(repository);
    }

    @Test
    public void jobCompletedWithErrorsTest() {
        var id = UUID.randomUUID();
        var event = new JobCompletedEvent();
        event.jobId = id;
        event.error = Optional.of("The data source cannot be read");
        event.location = Optional.of(URI.create("https://meinerezepte.local.lasagne.html"));

        var jobBeforeCompletion = new ImportJob();
        jobBeforeCompletion.setJobId(id);
        jobBeforeCompletion.setState(ImportJob.State.IN_PROGRESS);
        jobBeforeCompletion.setError(null);

        var jobAfterCompletion = new ImportJob();
        jobAfterCompletion.setJobId(id);
        jobAfterCompletion.setState(ImportJob.State.COMPLETED);
        jobAfterCompletion.setError(event.error.orElse(null));

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobBeforeCompletion));
        Mockito.doAnswer(x -> null).when(repository).update(jobAfterCompletion);

        Mockito.when(repository.findByIdOptional(id)).thenReturn(Optional.of(jobAfterCompletion));

        completed.fire(event);

        var j = sut.get(event.jobId);
        var expected = new ImportJob();
        expected.setJobId(id);
        expected.setState(ImportJob.State.COMPLETED);
        expected.setError(event.error.orElse(null));
        expected.setLocation(event.location.orElse(null));

        Assertions.assertEquals(expected, j.orElseThrow(AssertionFailedError::new));
        Assertions.assertEquals(expected.getError(), j.orElseThrow(AssertionFailedError::new).getError());

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

        List<JobReceivedEvent> getEvents() {
            return events;
        }
    }
}
