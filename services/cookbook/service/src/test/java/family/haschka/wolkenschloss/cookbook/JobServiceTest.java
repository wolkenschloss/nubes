package family.haschka.wolkenschloss.cookbook;

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
import java.io.FileNotFoundException;
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
    ImportJobRepository respository;

    @Inject
    JobService sut;

    @Inject
    JobReceivedObserver observer;

    @Test
    public void addJobTest() throws ExecutionException, InterruptedException {

        var id = UUID.randomUUID();

        // Mongo DB setzt die ID, wenn die Entität persistiert wird.
        Mockito.doAnswer(x -> {
            x.getArgument(0, ImportJob.class).jobId = id;
            return null;
        }).when(respository).persist(any(ImportJob.class));

        var job = new ImportJob();
        job.url = "https://meinerezepte.local/lasagen";
        job.jobId = id;

        var future = sut.addJob(job);

        future.thenAccept(event -> {

            Assertions.assertTrue(observer.events.contains(event));
//            Assertions.assertEquals(1, observer.getEvents().size());
            var expected = new JobReceivedEvent();
            expected.jobId = event.jobId;

            Assertions.assertEquals(expected, event);

        }).toCompletableFuture().get();

        Mockito.verify(respository, Mockito.times(1)).persist(job);
        Mockito.verifyNoMoreInteractions(respository);
    }

    @Inject
    Event<JobCompletedEvent> completed;

    @Test
    public void jobCompletedSuccessfullyTest() throws ExecutionException, InterruptedException {
        var id = UUID.randomUUID();
        var event = new JobCompletedEvent();
        event.jobId = id;
        event.location = Optional.of(URI.create("/recipe/123"));
        event.error = Optional.empty();

        var jobBeforeCompletion = new ImportJob();
        jobBeforeCompletion.jobId = id;
        jobBeforeCompletion.state = ImportJob.State.IN_PROGRESS;
        jobBeforeCompletion.location = Optional.empty();

        var jobAfterCompletion = new ImportJob();
        jobAfterCompletion.jobId = id;
        jobAfterCompletion.state = ImportJob.State.COMPLETED;
        jobAfterCompletion.location = event.location;

        Mockito.when(respository.findByIdOptional(id)).thenReturn(Optional.of(jobBeforeCompletion));
        Mockito.doAnswer(x -> null).when(respository).update(jobAfterCompletion);

        Mockito.when(respository.findByIdOptional(id)).thenReturn(Optional.of(jobAfterCompletion));

        var future = completed.fireAsync(event);

        future.thenAccept(e -> {
            var j = sut.get(e.jobId);
            var expected = new ImportJob();
            expected.jobId = id;
            expected.state = ImportJob.State.COMPLETED;
            expected.location = event.location;
            expected.error = Optional.empty();

            Assertions.assertEquals(expected, j.orElseThrow(AssertionFailedError::new));

        }).toCompletableFuture().get();

        Mockito.verify(respository, Mockito.times(2)).findByIdOptional(any());
        Mockito.verify(respository, Mockito.times(1)).update(any(ImportJob.class));
        Mockito.verifyNoMoreInteractions(respository);
    }

    @Test
    public void jobCompletedWithErrorsTest() throws ExecutionException, InterruptedException {
        var id = UUID.randomUUID();
        var event = new JobCompletedEvent();
        event.jobId = id;
        event.error = Optional.of(new FileNotFoundException());

        var jobBeforeCompletion = new ImportJob();
        jobBeforeCompletion.jobId = id;
        jobBeforeCompletion.state = ImportJob.State.IN_PROGRESS;
        jobBeforeCompletion.error = Optional.empty();

        var jobAfterCompletion = new ImportJob();
        jobAfterCompletion.jobId = id;
        jobAfterCompletion.state = ImportJob.State.COMPLETED;
        jobAfterCompletion.error = event.error;

        Mockito.when(respository.findByIdOptional(id)).thenReturn(Optional.of(jobBeforeCompletion));
        Mockito.doAnswer(x -> null).when(respository).update(jobAfterCompletion);

        Mockito.when(respository.findByIdOptional(id)).thenReturn(Optional.of(jobAfterCompletion));

        var future = completed.fireAsync(event);

        future.thenAccept(e -> {
            var j = sut.get(e.jobId);
            var expected = new ImportJob();
            expected.jobId = id;
            expected.state = ImportJob.State.COMPLETED;
            expected.error = event.error;

            Assertions.assertEquals(expected, j.orElseThrow(AssertionFailedError::new));
            Assertions.assertEquals(expected.error, j.orElseThrow(AssertionFailedError::new).error);

        }).toCompletableFuture().get();

        Mockito.verify(respository, Mockito.times(2)).findByIdOptional(any());
        Mockito.verify(respository, Mockito.times(1)).update(jobAfterCompletion);
        Mockito.verifyNoMoreInteractions(respository);
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
