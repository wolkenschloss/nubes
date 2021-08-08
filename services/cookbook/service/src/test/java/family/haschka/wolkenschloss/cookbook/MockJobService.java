package family.haschka.wolkenschloss.cookbook;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApplicationScoped
@Alternative
public class MockJobService implements IJobService {

    @Inject
    Logger log;

    @Override
    public CompletionStage<JobReceivedEvent> addJob(ImportJob job) {
        return null;
    }

    @Override
    public Optional<ImportJob> get(UUID id) {
        return Optional.empty();
    }

    @Override
    public void jobCompleted(JobCompletedEvent completed) {
        log.info("MockJobService: jobCompleted");
    }
}
