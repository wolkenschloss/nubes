package family.haschka.wolkenschloss.cookbook.job;

import java.net.URI;
import java.util.UUID;

public record JobCreatedEvent(UUID jobId, URI source) {
}
