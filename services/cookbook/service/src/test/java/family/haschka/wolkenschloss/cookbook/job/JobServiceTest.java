package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.RecipeService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

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
    Event<JobCompletedEvent> completed;

    @Inject
    ManagedExecutor executor;

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

        Mockito.when(repository.findByIdOptional(id))
                .thenReturn(Uni.createFrom().item(Optional.of(jobAfterCompletion)));

        var j = Uni.createFrom().completionStage(completed.fireAsync(event, NotificationOptions.ofExecutor(executor)))
                .flatMap(x -> sut.get(event.jobId()))
                .map(optional -> optional.orElseThrow(AssertionFailedError::new))
                .onFailure().invoke(failure -> System.out.println(failure.getMessage()));

        var expected = new ImportJob();
        expected.jobId = id;
        expected.state = State.COMPLETED;
        expected.location = event.location();
        expected.error = null;

        var subscriber = j.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertItem(expected);

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

        Mockito.when(repository.findByIdOptional(id))
                .thenReturn(Uni.createFrom().item(Optional.of(jobBeforeCompletion)));

        Mockito.when(repository.update(jobAfterCompletion))
                        .thenReturn(Uni.createFrom().item(jobAfterCompletion));

        Mockito.when(repository.findByIdOptional(id))
                .thenReturn(Uni.createFrom().item(Optional.of(jobAfterCompletion)));

        var j = Uni.createFrom().completionStage(completed.fireAsync(event, NotificationOptions.ofExecutor(executor)))
                .flatMap(e -> sut.get(e.jobId()))
                .map(optional -> optional.orElseThrow(AssertionFailedError::new));

        var expected = new ImportJob();
        expected.jobId = id;
        expected.state = State.COMPLETED;
        expected.error = event.error();
        expected.location = event.location();

        var subscriber = j.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.awaitItem().assertItem(expected);

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
            System.out.println("observeAsync JobReceivedEvent");
            events.add(event);
        }

        // Hm
        List<JobReceivedEvent> getEvents() {
            return events;
        }
    }
}
