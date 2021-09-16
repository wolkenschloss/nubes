package family.haschka.wolkenschloss.cookbook.job;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URI;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

public class JobServiceTest {

    @Test
    public void createJobTest() {

        ImportJobRepository repository = Mockito.mock(ImportJobRepository.class);
        EventBus eventBus = Mockito.mock(EventBus.class);
        IdentityGenerator identityGenerator = Mockito.mock(IdentityGenerator.class);
        Logger log = Mockito.mock(Logger.class);
        Message<URI> reply = Mockito.mock(Message.class);

        JobService service = new JobService(identityGenerator, repository, eventBus, log);

        UUID jobId = UUID.randomUUID();
        Mockito.when(identityGenerator.generate()).thenReturn(jobId);

        var job = ImportJob.create(jobId, URI.create("https://meinerezepte.local/lasagen"));
        var incomplete = job.located(URI.create("/recipe/123"));

        Mockito.when(repository.persist(any(ImportJob.class)))
                .thenReturn(Uni.createFrom().item(job));

        Mockito.when(reply.body())
                .thenReturn(incomplete.location);

        Mockito.when(repository.update(incomplete))
                .thenReturn(Uni.createFrom().item(incomplete));

        Mockito.when(eventBus.send(EventBusAddress.CREATED, new JobCreatedEvent(jobId, job.order)))
                .thenReturn(eventBus);

        service.create(job.order)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem().assertItem(job);

        Mockito.verify(identityGenerator, Mockito.times(1)).generate();

        Mockito.verify(repository, Mockito.times(1))
                .persist(job);

        Mockito.verify(eventBus, Mockito.times(1))
                .send(EventBusAddress.CREATED, new JobCreatedEvent(jobId, job.order));

        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(eventBus);
        Mockito.verifyNoMoreInteractions(identityGenerator);
        Mockito.verifyNoMoreInteractions(log);
    }

    @Test
    public void createJobPersistFailed() {
        ImportJobRepository repository = Mockito.mock(ImportJobRepository.class);
        EventBus eventBus = Mockito.mock(EventBus.class);
        IdentityGenerator identityGenerator = Mockito.mock(IdentityGenerator.class);
        Logger log = Mockito.mock(Logger.class);

        JobService service = new JobService(identityGenerator, repository, eventBus, log);

        UUID jobId = UUID.randomUUID();
        Mockito.when(identityGenerator.generate()).thenReturn(jobId);

        var job = ImportJob.create(jobId, URI.create("https://meinerezepte.local/lasagen"));

        Mockito.when(repository.persist(any(ImportJob.class)))
                .thenReturn(Uni.createFrom().failure(() -> new RuntimeException("Irgend ein Fehler")));

        service.create(job.order).log("test")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure().assertFailedWith(RuntimeException.class, "Irgend ein Fehler");

        Mockito.verify(identityGenerator, Mockito.times(1)).generate();
        Mockito.verify(repository, Mockito.times(1)).persist(job);

        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(eventBus);
        Mockito.verifyNoMoreInteractions(identityGenerator);
        Mockito.verifyNoMoreInteractions(log);
    }

    @Test
    public void sendEventFailed() {
        ImportJobRepository repository = Mockito.mock(ImportJobRepository.class);
        EventBus eventBus = Mockito.mock(EventBus.class);
        IdentityGenerator identityGenerator = Mockito.mock(IdentityGenerator.class);
        Logger log = Mockito.mock(Logger.class);

        JobService service = new JobService(identityGenerator, repository, eventBus, log);

        UUID jobId = UUID.randomUUID();
        Mockito.when(identityGenerator.generate()).thenReturn(jobId);

        var job = ImportJob.create(jobId, URI.create("https://meinerezepte.local/lasagen"));
        var failure = new RuntimeException("Ein Fehler ist aufgetreten");
        var failed = job.failed(failure);

        Mockito.when(repository.persist(any(ImportJob.class)))
                .thenReturn(Uni.createFrom().item(job));

        Mockito.when(repository.update(failed))
                .thenReturn(Uni.createFrom().item(failed));

//        var failure = new IllegalArgumentException("This operation is not possible")
        Mockito.when(eventBus.send(EventBusAddress.CREATED, new JobCreatedEvent(jobId, job.order)))
                .thenThrow(failure);

        service.create(job.order).log("test")
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure().assertFailedWith(failure.getClass(), failure.getMessage());

        Mockito.verify(identityGenerator, Mockito.times(1)).generate();

        Mockito.verify(repository, Mockito.times(1))
                .persist(job);

        Mockito.verify(eventBus, Mockito.times(1))
                .send(EventBusAddress.CREATED, new JobCreatedEvent(jobId, job.order));

        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(eventBus);
        Mockito.verifyNoMoreInteractions(identityGenerator);
        Mockito.verifyNoMoreInteractions(log);
    }

//    @Test void recipeParsedTest() {
//        ImportJobRepository repository = Mockito.mock(ImportJobRepository.class);
//        EventBus eventBus = Mockito.mock(EventBus.class);
//        IdentityGenerator identityGenerator = Mockito.mock(IdentityGenerator.class);
//        Logger log = Mockito.mock(Logger.class);
//
//        JobService service = new JobService(identityGenerator, repository, eventBus, log);
//
//        RecipeCreatedEvent event;
//        service.onRecipeCreated(event);
//
//    }

//    @Inject
//    EventBus bus;
//
//    @Test
//    public void jobCompletedSuccessfullyTest() {
//        var id = UUID.randomUUID();
//        var event = new JobCompletedEvent(id, URI.create("/recipe/123"), null);
//
//        var jobBeforeCompletion = new ImportJob();
//        jobBeforeCompletion.jobId = id;
//        jobBeforeCompletion.state = State.IN_PROGRESS;
//        jobBeforeCompletion.location = null;
//
//        var jobAfterCompletion = new ImportJob();
//        jobAfterCompletion.jobId = id;
//        jobAfterCompletion.state = State.COMPLETED;
//        jobAfterCompletion.location = event.location();
//
//        Mockito.when(repository.findByIdOptional(id))
//                .thenReturn(Uni.createFrom().item(Optional.of(jobBeforeCompletion)));
//
//        Mockito.when(repository.update(jobAfterCompletion))
//                .thenReturn(Uni.createFrom().item(jobAfterCompletion));
//
//        // when!
//        bus.send(EventBusAddress.COMPLETED, event);
//
//        var expected = new ImportJob();
//        expected.jobId = id;
//        expected.state = State.COMPLETED;
//        expected.location = event.location();
//        expected.error = null;
//
//        Mockito.verify(repository, Mockito.timeout(1000).times(1)).findByIdOptional(id);
//        Mockito.verify(repository, Mockito.timeout(1000).times(1)).update(expected);
//        Mockito.verifyNoMoreInteractions(repository);
//    }
//
//    @Test
//    public void jobCompletedWithErrorsTest() {
//        var id = UUID.randomUUID();
//        var event = new JobCompletedEvent(
//                id,
//                null,
//                "The data source cannot be read");
//
//        var jobBeforeCompletion = new ImportJob();
//        jobBeforeCompletion.jobId = id;
//        jobBeforeCompletion.state = State.IN_PROGRESS;
//        jobBeforeCompletion.error = null;
//        jobBeforeCompletion.location = null;
//        jobBeforeCompletion.order = null;
//
//        var jobAfterCompletion = new ImportJob();
//        jobAfterCompletion.jobId = id;
//        jobAfterCompletion.state = State.COMPLETED;
//        jobAfterCompletion.error = event.error();
//        jobAfterCompletion.location = null;
//        jobAfterCompletion.order = null;
//
//
//        Mockito.when(repository.findByIdOptional(id))
//                .thenReturn(Uni.createFrom().item(Optional.of(jobBeforeCompletion)));
//
//        Mockito.when(repository.update(jobAfterCompletion))
//                        .thenReturn(Uni.createFrom().item(jobAfterCompletion));
//
//        Mockito.when(repository.findByIdOptional(id))
//                .thenReturn(Uni.createFrom().item(Optional.of(jobAfterCompletion)));
//
//        bus.send(EventBusAddress.COMPLETED, event);
//
//        var expected = new ImportJob();
//        expected.jobId = id;
//        expected.state = State.COMPLETED;
//        expected.error = event.error();
//        expected.location = event.location();
//
//
//        Mockito.verify(repository, Mockito.timeout(1000).times(1)).findByIdOptional(id);
//        Mockito.verify(repository, Mockito.timeout(1000).times(1)).update(jobAfterCompletion);
//        Mockito.verifyNoMoreInteractions(repository);
//    }
}
