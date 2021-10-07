package family.haschka.wolkenschloss.cookbook.job;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.URISyntaxException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@DisplayName("Job Service")
public class JobServiceTest {

    private interface JobCreatedEventEmitter extends Emitter<JobCreatedEvent> {}

    @BeforeEach
    public void mockCollaborators() {
        emitter = Mockito.mock(JobCreatedEventEmitter.class);
        repository = Mockito.mock(ImportJobRepository.class);
        eventBus = Mockito.mock(EventBus.class);
        identityGenerator = Mockito.mock(IdentityGenerator.class);
        log = Mockito.mock(Logger.class);
    }

    private JobCreatedEventEmitter emitter = Mockito.mock(JobCreatedEventEmitter.class);
    private  ImportJobRepository repository = Mockito.mock(ImportJobRepository.class);
    private  EventBus eventBus = Mockito.mock(EventBus.class);
    private IdentityGenerator identityGenerator = Mockito.mock(IdentityGenerator.class);
    private Logger log = Mockito.mock(Logger.class);

    @Test
    @DisplayName("create should persist job")
    public void createJobTest() throws URISyntaxException {
        var testcase = JobFixture.LASAGNE.testcase();

        JobService service = new JobService(identityGenerator, repository, emitter, log);
        Mockito.when(identityGenerator.generate()).thenReturn(testcase.jobId());

        var job = testcase.job();

        Mockito.when(repository.persist(job))
                .thenReturn(Uni.createFrom().item(job));

        Mockito.when(emitter.send(testcase.created()))
                .thenReturn(CompletableFuture.allOf());

        service.create(testcase.order())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertItem(job);

        Mockito.verify(identityGenerator, Mockito.times(1)).generate();

        Mockito.verify(repository, Mockito.times(1))
                .persist(job);

        Mockito.verify(emitter, Mockito.times(1))
                        .send(testcase.created());
    }

    @AfterEach
    public void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(eventBus);
        Mockito.verifyNoMoreInteractions(identityGenerator);
        Mockito.verifyNoMoreInteractions(log);
    }

    @Test
    @DisplayName("job should not be created if persistence fails")
    public void createJobPersistFailed() throws URISyntaxException {

        var testcase = JobFixture.LASAGNE.testcase();
        JobService service = new JobService(identityGenerator, repository, emitter, log);

        Mockito.when(identityGenerator.generate()).thenReturn(testcase.jobId());

        var job = testcase.job();
        var failure = new RuntimeException("Irgend ein Fehler");

        Mockito.when(repository.persist(job))
                .thenReturn(Uni.createFrom().failure(failure));

        service.create(job.order)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure()
                .assertFailedWith(failure.getClass(), failure.getMessage());

        Mockito.verify(identityGenerator, Mockito.times(1)).generate();
        Mockito.verify(repository, Mockito.times(1)).persist(job);
    }

    @Test
    @DisplayName("import should fail if the event fails to send")
    public void sendEventFailed() throws URISyntaxException {

        var testcase = JobFixture.LASAGNE.testcase();
        JobService service = new JobService(identityGenerator, repository, emitter, log);

        Mockito.when(identityGenerator.generate()).thenReturn(testcase.jobId());

        var job = testcase.job();
        var failure = new RuntimeException("Ein Fehler ist aufgetreten");
        var failed = job.failed(failure);

        Mockito.when(repository.persist(job))
                .thenReturn(Uni.createFrom().item(job));

        Mockito.when(repository.update(failed))
                .thenReturn(Uni.createFrom().item(failed));

        Mockito.when(emitter.send(testcase.created()))
                .thenReturn(CompletableFuture.failedStage(failure));

        service.create(job.order)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertItem(job);

        Mockito.verify(identityGenerator, Mockito.times(1)).generate();

        Mockito.verify(repository, Mockito.times(1))
                .persist(job);

        Mockito.verify(repository, Mockito.times(1))
                        .update(failed);

        Mockito.verify(emitter, Mockito.times(1))
                        .send(testcase.created());

        Mockito.verify (log, Mockito.times(1))
                .infov("Error emitting JobCreatedEvent: {0}", failure.getMessage());
    }

    @Test
    @DisplayName("should update job if recipe was imported")
    public void updateOnImport() throws URISyntaxException {
        var testcase = JobFixture.LASAGNE.testcase();
        var job = testcase.job();

        Mockito.when(repository.findById(testcase.jobId()))
                .thenReturn(Uni.createFrom().item(job));

        var updated = job.located(testcase.location());
        Mockito.when(repository.update(updated))
                .thenReturn(Uni.createFrom().item(updated));

        var service = new JobService(identityGenerator, repository, emitter, log);
        var imported = testcase.imported();

        service.onRecipeImported(imported)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem();

        Mockito.verify(repository, Mockito.times(1)).findById(testcase.jobId());
        Mockito.verify(repository, Mockito.times(1)).update(updated);
    }

    @Test
    @DisplayName("should update job if recipe import failed")
    public void updateOnFailedImport() throws URISyntaxException {
        var testcase = JobFixture.LASAGNE.testcase();
        var job = testcase.job();

        Mockito.when(repository.findById(testcase.jobId()))
                .thenReturn(Uni.createFrom().item(job));

        var failure = new RuntimeException("Can not import Recipe");
        var failed = job.failed(failure);

        Mockito.when(repository.update(failed))
                .thenReturn(Uni.createFrom().item(failed));

        var service = new JobService(identityGenerator, repository, emitter, log);

        service.onImportFailed(testcase.failed(failure))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem();

        Mockito.verify(repository, Mockito.times(1)).findById(testcase.jobId());
        Mockito.verify(repository, Mockito.times(1)).update(failed);
    }

    @Test
    @DisplayName("should get job")
    public void shouldGetJob() throws URISyntaxException {
        var testcase = JobFixture.LASAGNE.testcase();
        var service  = new JobService(identityGenerator, repository, emitter, log);
        Mockito.when(repository.findByIdOptional(testcase.jobId()))
                        .thenReturn(Uni.createFrom().item(Optional.of(testcase.job())));

        service.get(testcase.jobId())
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem()
                .assertItem(Optional.of(testcase.job()));

        Mockito.verify(repository, Mockito.times(1))
                .findByIdOptional(testcase.jobId());
    }
}
