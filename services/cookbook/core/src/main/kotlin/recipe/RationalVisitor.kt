package family.haschka.wolkenschloss.cookbook.recipe

import family.haschka.wolkenschloss.cookbook.parser.IngredientParserBaseVisitor
import family.haschka.wolkenschloss.cookbook.parser.IngredientParser

class RationalVisitor : IngredientParserBaseVisitor<Rational>() {

    override fun visitMixed_fraction(ctx: IngredientParser.Mixed_fractionContext): Rational {
        val sign = visitSign(ctx.sign())
        val number = visitNumber(ctx.number())
        val fraction = visitRational(ctx.rational())

        return sign * (number + fraction)
    }

    override fun visitQuantity(ctx: IngredientParser.QuantityContext): Rational {
        val number = visitNumber(ctx.number())
        val fraction = visitRational(ctx.rational())

        return number + fraction
    }

    override fun visitSign(ctx: IngredientParser.SignContext?): Rational {
        if (ctx == null) {
            return Rational(1)
        }

        return Rational(-1)
    }

    override fun visitNumber(ctx: IngredientParser.NumberContext?): Rational {
        if (ctx == null) {
            return Rational(0)
        }

        return Rational(ctx.UINT().text.toInt())
    }

    override fun visitNumerator(ctx: IngredientParser.NumeratorContext): Rational {
        return Rational(ctx.UINT().text.toInt())
    }

    override fun visitDenominator(ctx: IngredientParser.DenominatorContext): Rational {
        return Rational(1, ctx.UINT().text.toInt())
    }

    override fun visitRational(ctx: IngredientParser.RationalContext?): Rational {
        if (ctx == null) {
            return Rational(0)
        }

        if (ctx.SYMBOL() != null) {
            return Rational.fractions.getValue(ctx.SYMBOL().text)
        }

        val numerator = visitNumerator(ctx.numerator())
        val denominator = visitDenominator(ctx.denominator())

        return numerator * denominator
    }
}
