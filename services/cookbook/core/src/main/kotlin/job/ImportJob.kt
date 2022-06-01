package family.haschka.wolkenschloss.cookbook.job

import java.net.URI
import java.util.*

data class ImportJob(val jobId: UUID, val order: URI, val state: State, val location: URI, val error: String) {

    fun complete(location: URI): ImportJob =
        this.copy(location = location, state = State.COMPLETED)

    fun located(location: URI): ImportJob =
        this.copy(location = location, state = State.INCOMPLETE)

    fun failed(failure: Throwable) =
        this.copy(state = State.FAILED, error = failure.message ?: "Unknown failure")

    companion object {
        @JvmStatic
        fun create(jobId: UUID, order: URI): ImportJob =
            ImportJob(jobId, order, State.CREATED, NoLocation, "")

        @JvmStatic val NoLocation: URI = URI.create("#")
    }
}
