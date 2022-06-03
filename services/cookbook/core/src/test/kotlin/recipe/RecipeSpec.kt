package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RecipeSpec : FunSpec({

    test("It should be possible to scale the servings of a recipe") {
        val recipe = Recipe("123", "Pure Water", "Fill pure water into a glass",
        listOf(Ingredient(Rational(300), "ml", "Water")),
        Servings(3),
        0
        )

        val scaledRecipe = recipe.scale(Servings(4))

        scaledRecipe shouldBe Recipe("123", "Pure Water", "Fill pure water into a glass",
        listOf(Ingredient(Rational(400), "ml", "Water")),
        Servings(4), 0)

    }
})