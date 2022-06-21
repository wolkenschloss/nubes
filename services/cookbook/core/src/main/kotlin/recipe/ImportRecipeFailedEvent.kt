package family.haschka.wolkenschloss.cookbook.recipe

import family.haschka.wolkenschloss.cookbook.Event
import java.util.UUID

@Event
data class ImportRecipeFailedEvent(val uuid: UUID,val cause:  Throwable )
