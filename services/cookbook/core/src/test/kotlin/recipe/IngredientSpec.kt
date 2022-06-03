package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class IngredientSpec : FunSpec({


    context("Parsing ingredient string") {
        withData(
            mapOf(
                "250 g Mehl" to Ingredient(Rational(250), "g", "Mehl"),
                "1 kg Potatoes" to Ingredient(Rational(1), "kg", "Potatoes"),
                "125 ml Milk" to Ingredient(Rational(125), "ml", "Milk"),
                "1 l Water" to Ingredient(Rational(1), "l", "Water"),
                "1 cl Rum" to Ingredient(Rational(1), "cl", "Rum"),
                "42 Zutat ohne Einheit" to Ingredient(Rational(42), null, "Zutat ohne Einheit"),
                "42 1/2 Zutat ohne Einheit" to Ingredient(Rational(85, 2), null, "Zutat ohne Einheit"),
                "42½ Zutat ohne Einheit" to Ingredient(Rational(85, 2), null, "Zutat ohne Einheit"),
                "Zutat ohne Menge und Einheit" to Ingredient(null, null, "Zutat ohne Menge und Einheit"),
                "g name of ingredient" to Ingredient(null, "g", "name of ingredient"),
                "g" to Ingredient(null, null, "g"),
                "1/2 cl Rum" to Ingredient(Rational(1, 2), "cl", "Rum"),
                "1/2 g Mehl" to Ingredient(Rational(1, 2), "g", "Mehl"),
                "1 1/2 kg Mehl" to Ingredient(Rational(3, 2), "kg", "Mehl"),
                "1 1/2kg Mehl" to Ingredient(Rational(3, 2), "kg", "Mehl"),
                "⅛ l Sahne" to Ingredient(Rational(1, 8), "l", "Sahne"),
                "1 ⅛ l Sahne" to Ingredient(Rational(9, 8), "l", "Sahne"),
                "1⅛ l Sahne" to Ingredient(Rational(9, 8), "l", "Sahne"),
                "20g Moon Sugar" to Ingredient(Rational(20), "g", "Moon Sugar"),
                "2⅒g Moon Sugar" to Ingredient(Rational(21, 10), "g", "Moon Sugar")
            )
        ) { ingredient ->
            Ingredient.parse(this.testCase.name.testName) shouldBe ingredient
        }
    }

    data class ServingsTestcase(val ingredient: Ingredient, val factor: Rational, val expected: Ingredient)

    context("Scale servings") {
        withData(
            ServingsTestcase(
                Ingredient(Rational(2), null, "Onions"),
                Rational(5, 4),
                Ingredient(Rational(5, 2), null, "Onions")
            ),
            ServingsTestcase(
                Ingredient(Rational(800), "g", "Tomatoes"),
                Rational(2, 3),
                Ingredient(Rational(1600, 3), "g", "Tomatoes")
            )
        ) { testcase ->
            testcase.ingredient.scale(testcase.factor) shouldBe testcase.expected
        }
    }

    context("Print servings") {
        withData(
            mapOf(
                "2 ½ St. Zwiebeln" to Ingredient(Rational(5, 2), "St.", "Zwiebeln"),
                "Rotwein" to Ingredient(null, null, "Rotwein"),
                "42 Blaubeeren" to Ingredient(Rational(42), null, "Blaubeeren")
            )
        ) { ingredient ->
            ingredient.toString() shouldBe this.testCase.name.testName
        }
    }
})