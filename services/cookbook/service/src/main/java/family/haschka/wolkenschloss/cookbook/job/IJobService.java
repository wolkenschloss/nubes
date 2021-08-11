package family.haschka.wolkenschloss.cookbook.job;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public interface IJobService {
    CompletionStage<JobReceivedEvent> addJob(ImportJob job);

    Optional<ImportJob> get(UUID id);

    void jobCompleted(JobCompletedEvent completed);
}
