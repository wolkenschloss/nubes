package family.haschka.wolkenschloss.cookbook;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class JobReceivedEvent {
    public UUID jobId;
    public URI source;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobReceivedEvent that = (JobReceivedEvent) o;
        return Objects.equals(jobId, that.jobId) && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId, source);
    }

    @Override
    public String toString() {
        return "JobReceivedEvent{" +
                "jobId=" + jobId +
                ", source=" + source +
                '}';
    }
}
