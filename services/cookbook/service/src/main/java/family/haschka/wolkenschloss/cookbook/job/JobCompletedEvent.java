package family.haschka.wolkenschloss.cookbook.job;

import java.net.URI;
import java.util.UUID;

public record JobCompletedEvent(UUID jobId, URI location, String error) {

}
