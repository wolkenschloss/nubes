package family.haschka.wolkenschloss.cookbook.recipe

data class Recipe(
    val _id: String,
    val title: String,
    val  preparation: String?,
    val ingredients: List<Ingredient>,
    val servings: Servings,
    val created:  Long) {

    fun  scale(servings: Servings): Recipe {
        val factor = Rational(servings.amount, this.servings.amount)
        return this.copy(ingredients = this.ingredients.map { i -> i.scale(factor) }, servings = servings)
    }
}
