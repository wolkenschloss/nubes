package family.haschka.wolkenschloss.cookbook.ingredient

import java.util.*

data class RecipeAddedEvent(val recipeId: UUID, val ingredients: ArrayList<Ingredient>)
