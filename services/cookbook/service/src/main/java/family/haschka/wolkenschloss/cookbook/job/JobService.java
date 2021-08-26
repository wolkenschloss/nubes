package family.haschka.wolkenschloss.cookbook.job;

import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JobService {
    @Inject
    Event<JobReceivedEvent> received;

    @Inject
    ImportJobRepository repository;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    Logger log;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    ManagedExecutor executor;

    public Uni<JobReceivedEvent> addJob(ImportJob job) {
        return repository.persist(job)
                .map(j -> new JobReceivedEvent(j.jobId, URI.create(j.order)))
                .flatMap(e -> Uni.createFrom()
                        .completionStage(() -> received.fireAsync(e, NotificationOptions.ofExecutor(executor)))
                        .map(x -> e));
    }

    public Uni<Optional<ImportJob>> get(UUID id) {
        return repository.findByIdOptional(id);
    }

    public void jobCompleted(@ObservesAsync JobCompletedEvent event) {
        var result = repository.findByIdOptional(event.jobId())
                .map(optional -> optional.orElseThrow(NotFoundException::new))
                .map(job -> job.complete(event.location(), event.error()))
                .flatMap(completed -> repository.update(completed));

        result.await().atMost(Duration.ofSeconds(4));
    }
}
