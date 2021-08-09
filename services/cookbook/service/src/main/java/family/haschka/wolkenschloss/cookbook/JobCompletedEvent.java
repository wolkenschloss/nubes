package family.haschka.wolkenschloss.cookbook;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class JobCompletedEvent {
    public UUID jobId;
    public Optional<URI> location;
    public Optional<String> error;

    @Override
    public String toString() {
        return "JobCompletedEvent{" +
                "jobId=" + jobId +
                ", location=" + location +
                ", error=" + error +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobCompletedEvent that = (JobCompletedEvent) o;
        return Objects.equals(jobId, that.jobId) && Objects.equals(location, that.location) && Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, location, error);
    }
}
