package family.haschka.wolkenschloss.cookbook.ingredient

import java.util.*

data class IngredientAddedEvent(val recipeId: UUID, val ingredient: Ingredient)
