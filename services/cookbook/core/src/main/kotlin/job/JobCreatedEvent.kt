package family.haschka.wolkenschloss.cookbook.job

import java.net.URI
import java.util.UUID

data class JobCreatedEvent(val jobId: UUID,val source: URI)
