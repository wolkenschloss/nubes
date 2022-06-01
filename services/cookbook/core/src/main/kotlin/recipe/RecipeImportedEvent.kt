package family.haschka.wolkenschloss.cookbook.recipe

import java.net.URI
import java.util.*

data class RecipeImportedEvent(val jobId: UUID, val location: URI)
