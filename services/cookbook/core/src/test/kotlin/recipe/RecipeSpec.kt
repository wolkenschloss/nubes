package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class RecipeSpec : FunSpec({

    test("It should be possible to scale the servings of a recipe") {
        val recipe = Recipe("123", "Pure Water", "Fill pure water into a glass",
        listOf(Ingredient("Water", Rational(300), "ml")),
        Servings(3),
        0
        )

        val scaledRecipe = recipe.scale(Servings(4))

        scaledRecipe shouldBe Recipe("123", "Pure Water", "Fill pure water into a glass",
        listOf(Ingredient("Water", Rational(400), "ml")),
        Servings(4), 0)

    }
})