package family.haschka.wolkenschloss.cookbook.job;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

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

    public CompletionStage<JobReceivedEvent> addJob(ImportJob job) {
        log.info("JobService.addJob start");
        repository.persist(job);

        log.info(job);
        var event = new JobReceivedEvent(job.getJobId(), URI.create(job.getUrl()));
        log.info("JobService.addJob end");
        return received.fireAsync(event, NotificationOptions.ofExecutor(executor));
    }

    public Optional<ImportJob> get(UUID id) {
        return repository.findByIdOptional(id);
    }

    public void jobCompleted(@Observes JobCompletedEvent completed) {

        log.info("handle job completed event");
        var job = repository.findByIdOptional(completed.jobId()).orElseThrow(NotFoundException::new);
        job.setState(ImportJob.State.COMPLETED);
        job.setLocation(completed.location());
        job.setError(completed.error());

        repository.update(job);
        log.info("updated job");
    }
}
