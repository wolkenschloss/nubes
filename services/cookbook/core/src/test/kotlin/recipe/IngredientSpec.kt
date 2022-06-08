package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class IngredientSpec : FunSpec({

    context("Parsing ingredient string") {
        withData(
            mapOf(
                "1 Dose Tomaten, geschälte (800g)" to Ingredient("Tomaten, geschälte (800g)", Rational(1), "Dose"),
                "250 g Mehl" to Ingredient("Mehl", Rational(250), "g"),
                "1 kg Potatoes" to Ingredient("Potatoes", Rational(1), "kg"),
                "125 ml Milk" to Ingredient("Milk", Rational(125), "ml"),
                "1 l Water" to Ingredient("Water", Rational(1), "l"),
                "1 cl Rum" to Ingredient("Rum", Rational(1), "cl"),
                "42 Zutat ohne Einheit" to Ingredient("Zutat ohne Einheit", Rational(42), null),
                "42 1/2 Zutat ohne Einheit" to Ingredient("Zutat ohne Einheit", Rational(85, 2), null),
                "42½ Zutat ohne Einheit" to Ingredient("Zutat ohne Einheit", Rational(85, 2), null),
                "Zutat ohne Menge und Einheit" to Ingredient("Zutat ohne Menge und Einheit", null, null),
                "g name of ingredient" to Ingredient("g name of ingredient", null, null),
                "g" to Ingredient("g", null, null),
                "1/2 cl Rum" to Ingredient("Rum", Rational(1, 2), "cl"),
                "1/2 g Mehl" to Ingredient("Mehl", Rational(1, 2), "g"),
                "1 1/2 kg Mehl" to Ingredient("Mehl", Rational(3, 2), "kg"),
                "1 1/2kg Mehl" to Ingredient("Mehl", Rational(3, 2), "kg"),
                "⅛ l Sahne" to Ingredient("Sahne", Rational(1, 8), "l"),
                "1 ⅛ l Sahne" to Ingredient("Sahne", Rational(9, 8), "l"),
                "1⅛ l Sahne" to Ingredient("Sahne", Rational(9, 8), "l"),
                "20g Moon Sugar" to Ingredient("Moon Sugar", Rational(20), "g"),
                "2⅒g Moon Sugar" to Ingredient("Moon Sugar", Rational(21, 10), "g"),
                "1 1/2 kg (kl.) Tomaten" to Ingredient("(kl.) Tomaten", Rational(3, 2), "kg"),
                "13 mg Ąņŷ pøśŝīble \uD83D\uDC7D unicode character for name \uD83D\uDCA9" to Ingredient(
                    "Ąņŷ pøśŝīble \uD83D\uDC7D unicode character for name \uD83D\uDCA9",
                    Rational(13),
                    "mg"
                )
            )
        ) { ingredient ->

            Ingredient.parse(this.testCase.name.testName).name shouldBe ingredient.name
            Ingredient.parse(this.testCase.name.testName) shouldBe ingredient
        }
    }

    context("Parse ingredient with all known units") {
        withData(Unit.values().flatMap { listOf(it.unit, *it.aliases) }) {
            val input = "1 1/2 $it (kl.) Dinger"
            Ingredient.parse(input).name shouldBe "(kl.) Dinger"
            Ingredient.parse(input) shouldBe Ingredient("(kl.) Dinger", Rational(3, 2), it)
        }
    }

    data class ServingsTestcase(val ingredient: Ingredient, val factor: Rational, val expected: Ingredient)

    context("Scale servings") {
        withData(
            ServingsTestcase(
                Ingredient("Onions", Rational(2), null),
                Rational(5, 4),
                Ingredient("Onions", Rational(5, 2), null)
            ),
            ServingsTestcase(
                Ingredient("Tomatoes", Rational(800), "g"),
                Rational(2, 3),
                Ingredient("Tomatoes", Rational(1600, 3), "g")
            )
        ) { testcase ->
            testcase.ingredient.scale(testcase.factor) shouldBe testcase.expected
        }
    }

    context("Print servings") {
        withData(
            mapOf(
                "2 ½ St. Zwiebeln" to Ingredient("Zwiebeln", Rational(5, 2), "St."),
                "Rotwein" to Ingredient("Rotwein", null, null),
                "42 Blaubeeren" to Ingredient("Blaubeeren", Rational(42), null)
            )
        ) { ingredient ->
            ingredient.toString() shouldBe this.testCase.name.testName
        }
    }
})