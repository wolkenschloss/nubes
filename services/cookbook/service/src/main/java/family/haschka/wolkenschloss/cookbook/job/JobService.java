package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.job.*;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class JobService implements IJobService {
    @Inject
    Event<JobReceivedEvent> received;

    @Inject
    ImportJobRepository repository;

    @Inject
    Logger log;

    @Inject
    ManagedExecutor executor;

    @Override
    public CompletionStage<JobReceivedEvent> addJob(ImportJob job) {
        log.info("JobService.addJob start");
        repository.persist(job);

        log.info(job);
        var event = new JobReceivedEvent();
        event.jobId = job.getJobId();
        event.source = URI.create(job.getUrl());
        log.info("JobService.addJob end");
        return received.fireAsync(event, NotificationOptions.ofExecutor(executor));
    }

    @Override
    public Optional<ImportJob> get(UUID id) {
        return repository.findByIdOptional(id);
    }

    @Override
    public void jobCompleted(JobCompletedEvent completed) {
        log.info("handle job completed event");
        var job = repository.findByIdOptional(completed.jobId).orElseThrow(NotFoundException::new);
        job.setState(ImportJob.State.COMPLETED);
        job.setLocation(completed.location.orElse(null));
        job.setError(completed.error.orElse(null));

        repository.update(job);
        log.info("updated job");
    }
}
