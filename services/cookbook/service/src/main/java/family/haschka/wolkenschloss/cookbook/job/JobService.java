package family.haschka.wolkenschloss.cookbook.job;

import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JobService {

    @Inject
    ImportJobRepository repository;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    ManagedExecutor executor;

    @Inject
    EventBus eventBus;


    public Uni<JobReceivedEvent> addJob(ImportJob job) {
        return repository.persist(job)
                .map(j -> new JobReceivedEvent(j.jobId, URI.create(j.order)))
                .invoke(event -> eventBus.send("job-received", event));
    }

    public Uni<Optional<ImportJob>> get(UUID id) {
        return repository.findByIdOptional(id);
    }

    @Inject
    Logger log;
    @ConsumeEvent("job.completed")
    public void onCompleted(JobCompletedEvent event) {
        log.infov("onCompleted({0})", event);

        var result = repository.findByIdOptional(event.jobId())
                .map(optional -> optional.orElseThrow(NotFoundException::new))
                .map(job -> job.complete(event.location(), event.error()))
                .log("onComplete")
                .flatMap(job -> repository.update(job))
                .log("after update")
//                .flatMap(x -> repository.findById(x.jobId))
                .subscribe()
                .with(
                        job -> log.infov("Completed Job: {0}", job),
                        failure -> log.warnv("Failure: {0}", failure));

//                .subscribe().with(
//                        job -> {repository.update(job); log.infov("job updated: {0}", job);},
//                        error -> log.infov("Unable to complete job {0}: {1}", event.jobId(), error));
    }
}
