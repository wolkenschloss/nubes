package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.ImportRecipeFailedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeImportedEvent;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JobService {

    JobService(IdentityGenerator identityGenerator, ImportJobRepository repository, @Channel("job-created")Emitter<JobCreatedEvent> emitter, Logger log) {
        this.identityGenerator = identityGenerator;
        this.repository = repository;
        this.log = log;
        this.emitter = emitter;
    }

    private final ImportJobRepository repository;
    private final Logger log;
    private final IdentityGenerator identityGenerator;
    private final Emitter<JobCreatedEvent> emitter;

    @Incoming("recipe-imported")
    public Uni<Void> onRecipeImported(RecipeImportedEvent event) {

        return repository.findById(event.getJobId())
                .map(job -> job.located(event.getLocation()))
                .chain(repository::update)
                .log("updated")
                .onItem().ignore().andContinueWithNull();
    }

    @Incoming("import-failed")
    public Uni<Void> onImportFailed(ImportRecipeFailedEvent event) {
        return repository.findById(event.getUuid())
                .chain(job -> repository.update(job.failed(event.getCause())))
                .onItem().ignore().andContinueWithNull();
    }

    public Uni<ImportJob> create(URI order) {
        var job = ImportJob.create(identityGenerator.generate(), order);
        return repository.persist(job)
                .ifNoItem().after(Duration.ofMillis(1000)).fail()
                .invoke(created -> {
                    var e = new JobCreatedEvent(created.getJobId(), created.getOrder());
                    var cs = emitter.send(e);
                    cs.exceptionally(failure -> {
                        log.infov("Error emitting JobCreatedEvent: {0}", failure.getMessage());
                        repository.update(job.failed(failure)).subscribe().asCompletionStage();
                        return null;
                    });
                });
    }

    public Uni<Optional<ImportJob>> get(UUID id) {
        return repository.findByIdOptional(id);
    }
}
