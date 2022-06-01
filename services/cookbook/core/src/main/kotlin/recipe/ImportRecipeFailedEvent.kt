package family.haschka.wolkenschloss.cookbook.recipe

import java.util.UUID

data class ImportRecipeFailedEvent(val uuid: UUID,val cause:  Throwable )
