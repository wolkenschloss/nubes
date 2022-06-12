package family.haschka.wolkenschloss.cookbook.recipe

import family.haschka.wolkenschloss.cookbook.parser.IngredientParserBaseVisitor
import family.haschka.wolkenschloss.cookbook.parser.IngredientParser

class IngredientBuilder : IngredientParserBaseVisitor<Ingredient>() {

    override fun visitLine(ctx: IngredientParser.LineContext): Ingredient {

        if (ctx.name() == null) {
            return Ingredient("")
        }

        val quant = ctx.quantity()?.let { this.visitQuantity(ctx.quantity()) } ?: Ingredient("")

        return Ingredient(
            this.visitName(ctx.name()).name,
            quant.quantity,
            ctx.UNIT()?.text
        )
    }

    override fun visitQuantity(ctx: IngredientParser.QuantityContext): Ingredient {
        val builder = RationalBuilder()
        val quantity = builder.visitQuantity(ctx)

        if (quantity == Rational(0)) {
            return Ingredient("")
        }

        return Ingredient("", quantity)
    }

    override fun visitName(ctx: IngredientParser.NameContext): Ingredient {
        val text = (ctx.TEXT_START()?.text ?: "") + (ctx.REST()?.text ?: "")
        return Ingredient(text)
    }
}