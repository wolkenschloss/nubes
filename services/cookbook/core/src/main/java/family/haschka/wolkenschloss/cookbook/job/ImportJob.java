package family.haschka.wolkenschloss.cookbook.job;

import java.net.URI;
import java.util.UUID;

public record ImportJob(UUID jobId, URI order, State state, URI location, String error) {

    @Override
    public String toString() {
        return "ImportJob{" +
                "url='" + order + '\'' +
                ", jobId=" + jobId +
                ", state=" + state +
                ", location=" + location +
                ", error=" + error +
                '}';
    }

    public ImportJob complete(URI location, String error) {
        return new ImportJob(jobId, order, State.COMPLETED, location, error);
    }

    public ImportJob located(URI location) {
        return new ImportJob(jobId, order, State.INCOMPLETE, location, error);
    }

    public static ImportJob create(UUID jobId, URI order) {
        return new ImportJob(jobId, order, State.CREATED, null, null);
    }

    public ImportJob failed(Throwable failure) {
        return new ImportJob(jobId, order, State.FAILED, location, failure.getMessage());
    }
}
