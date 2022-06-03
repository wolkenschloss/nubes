package family.haschka.wolkenschloss.cookbook.ingredient

data class IngredientRequiredEvent(val recipeId: String, val ingredient: String) {

    override fun toString(): String {
        return "IngredientRequiredEvent[" +
                "recipeId=" + recipeId + ", " +
                "ingredient=" + ingredient + ']'
    }
}