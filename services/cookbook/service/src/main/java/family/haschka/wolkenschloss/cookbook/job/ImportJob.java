package family.haschka.wolkenschloss.cookbook.job;

import org.bson.codecs.pojo.annotations.BsonId;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class ImportJob {
    @BsonId
    public UUID jobId;

    public String order;
    public State state;
    public URI location;
    public String error;

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
        this.location = location;
        this.error = error;
        this.state = State.COMPLETED;

        return this;
    }

}
