package family.haschka.wolkenschloss.cookbook.job

import java.net.URI
import java.util.*

data class JobCompletedEvent(val jobId: UUID, val location: URI, val error: String)
