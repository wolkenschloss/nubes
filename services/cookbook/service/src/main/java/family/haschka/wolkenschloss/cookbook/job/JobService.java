package family.haschka.wolkenschloss.cookbook.job;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JobService {

    @Inject
    ImportJobRepository repository;

    @Inject
    EventBus eventBus;

    @Inject
    Logger log;

    // TODO: Wäre es nicht besser, ImportJob statt JobReceivedEvent zurückzugeben?
    public Uni<JobReceivedEvent> create(ImportJob job) {
        return repository.persist(job)
                .map(j -> new JobReceivedEvent(j.jobId, URI.create(j.order)))
                .invoke(event -> eventBus.send(EventBusAddress.RECEIVED, event));
    }

    public Uni<Optional<ImportJob>> get(UUID id) {
        return repository.findByIdOptional(id);
    }

    @ConsumeEvent(EventBusAddress.COMPLETED)
    public void onCompleted(JobCompletedEvent event) {
        repository.findByIdOptional(event.jobId())
                .map(optional -> optional.orElseThrow(NotFoundException::new))
                .map(job -> job.complete(event.location(), event.error()))
                .flatMap(job -> repository.update(job))
                .subscribe()
                .with(
                        job -> log.infov("Job '{0}' completed", job.jobId),
                        failure -> log.warnv("Failure: {0}", failure));
    }
}
