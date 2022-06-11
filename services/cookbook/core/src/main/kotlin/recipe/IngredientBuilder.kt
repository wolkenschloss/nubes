package family.haschka.wolkenschloss.cookbook.recipe

import family.haschka.wolkenschloss.cookbook.parser.IngredientBaseVisitor
import family.haschka.wolkenschloss.cookbook.parser.IngredientParser

class IngredientBuilder : IngredientBaseVisitor<Ingredient>() {

    override fun visitLine(ctx: IngredientParser.LineContext): Ingredient {

        if (ctx.name() == null) {
            return Ingredient("", null, null)
        }

        val quant = ctx.quantity()?.let { this.visitQuantity(ctx.quantity()) } ?: Ingredient("", null, null)

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
            println("visit quantity -> 0 -> null")
            return Ingredient("", null, null)
        }

        return Ingredient("", quantity, null)
    }

    override fun visitName(ctx: IngredientParser.NameContext): Ingredient {
        val ingredient = Ingredient(ctx.NAME().text, null, null)

        ctx.name()?.let {
            return Ingredient(
                visitName(ctx.name()).name + " " + ingredient.name, null,
                null
            )
        }

        return ingredient
    }
}