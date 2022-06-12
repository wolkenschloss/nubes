package family.haschka.wolkenschloss.cookbook.recipe

import family.haschka.wolkenschloss.cookbook.parser.IngredientLexer
import family.haschka.wolkenschloss.cookbook.parser.IngredientParser
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

class IngredientParserSpec : FunSpec({
    val data = mapOf(
        "X" to Ingredient("X"),
        "200 g Mehl" to Ingredient("Mehl", Rational(200), "g"),
        "1/2 l Wasser" to Ingredient("Wasser", Rational(1, 2), "l"),
        "1 Dose Tomaten, geschälte (800g)" to Ingredient("Tomaten, geschälte (800g)", Rational(1), "Dose"),
        "4 EL getrocknete italienische Kräuter" to Ingredient("getrocknete italienische Kräuter", Rational(4), "EL")
    )

    context("parse ingredient with visitor") {
        withData(data) {
            val input = CharStreams.fromString(this.testCase.name.testName)
            val lexer = IngredientLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = IngredientParser(tokens)
            val tree = parser.line()

            val ingredient = IngredientBuilder().visit(tree)
            parser.numberOfSyntaxErrors shouldBe 0
            ingredient shouldBe it
        }
    }

    context("parser should report syntax errors") {
        withData(
            "- Mondzucker" to listOf("extraneous input '-' expecting {<EOF>, UINT, SYMBOL, TEXT_START, REST}"),
            "5- Waffeln" to listOf("no viable alternative at input '5-'"),
            "-4 2/3 kg Heuschrecken" to listOf("extraneous input '-' expecting {<EOF>, UINT, SYMBOL, TEXT_START, REST}"),
            "p Dingens" to emptyList()
        ) {
            val input = CharStreams.fromString(it.first)
            val lexer = IngredientLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = IngredientParser(tokens)
            val listener = SyntaxErrorListener()
            parser.addErrorListener(listener)
            parser.line()
            parser.numberOfSyntaxErrors shouldBe it.second.size
            listener.errors shouldContainExactly it.second
        }
    }
})