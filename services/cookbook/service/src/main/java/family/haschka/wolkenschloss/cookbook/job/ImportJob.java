package family.haschka.wolkenschloss.cookbook.job;

import org.bson.codecs.pojo.annotations.BsonId;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class ImportJob {
    @BsonId
    public UUID jobId;

    public URI order;
    public State state;
    public URI location;
    public String error;

    // Required by JSON-B
    public ImportJob() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportJob importJob = (ImportJob) o;
        return Objects.equals(order, importJob.order) && Objects.equals(jobId, importJob.jobId) && state == importJob.state && Objects.equals(location, importJob.location) && Objects.equals(error, importJob.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, jobId, state, location, error);
    }

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
        ImportJob copy = new ImportJob();
        copy.location = location;
        copy.error = error;
        copy.state = State.COMPLETED;
        copy.order = order;
        copy.jobId = jobId;

        return copy;
    }

    public ImportJob located(URI location) {
        ImportJob copy = new ImportJob();
        copy.location = location;
        copy.error = error;
        copy.state = State.INCOMPLETE;
        copy.order = order;
        copy.jobId = jobId;

        return copy;
    }

    public static ImportJob create(UUID jobId, URI order) {
        var job = new ImportJob();
        job.jobId = jobId;
        job.order = order;
        job.state = State.CREATED;

        return job;
    }

    public ImportJob failed(Throwable failure) {
        var job = new ImportJob();
        job.jobId = jobId;
        job.order = order;
        job.state = State.FAILED;
        job.error = failure.getMessage();

        return job;
    }
}
