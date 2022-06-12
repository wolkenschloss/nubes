package family.haschka.wolkenschloss.cookbook.recipe

import family.haschka.wolkenschloss.cookbook.parser.IngredientLexer
import family.haschka.wolkenschloss.cookbook.parser.IngredientParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream

// TODO: Add the property "malformed" if the ingredient was parsed with a syntax error
data class Ingredient(val name: String, val quantity: Rational? = null, val unit: String? = null) {

    fun scale(factor: Rational): Ingredient = Ingredient(
        name,
        quantity?.let { it * factor },
        unit
    )

    override fun toString(): String = listOfNotNull(quantity, unit, name)
        .joinToString(" ") { obj: Any -> obj.toString() }

    companion object {
        @JvmStatic
        fun parse(string: String): Ingredient {
            val input = CharStreams.fromString(string)
            val lexer = IngredientLexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = IngredientParser(tokens)
            val tree = parser.line()

            if (parser.numberOfSyntaxErrors > 0) {
                return Ingredient(string)
            }

            return IngredientBuilder().visit(tree)
        }
    }
}