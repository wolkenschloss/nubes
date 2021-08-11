package family.haschka.wolkenschloss.cookbook.job;

import org.bson.codecs.pojo.annotations.BsonId;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

public class ImportJob {
    @BsonId
    private UUID jobId;

    private String url;
    private State state;
    private URI location;
    private String error;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImportJob importJob = (ImportJob) o;
        return Objects.equals(url, importJob.url) && Objects.equals(jobId, importJob.jobId) && state == importJob.state && Objects.equals(location, importJob.location) && Objects.equals(error, importJob.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, jobId, state, location, error);
    }

    @Override
    public String toString() {
        return "ImportJob{" +
                "url='" + url + '\'' +
                ", jobId=" + jobId +
                ", state=" + state +
                ", location=" + location +
                ", error=" + error +
                '}';
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public URI getLocation() {
        return location;
    }

    public void setLocation(URI location) {
        this.location = location;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public enum State {IN_PROGRESS, COMPLETED}
}
