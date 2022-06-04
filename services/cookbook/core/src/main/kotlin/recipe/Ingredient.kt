package family.haschka.wolkenschloss.cookbook.recipe

import java.util.*
import java.util.regex.Pattern

data class Ingredient(val quantity: Rational? = null, val unit: String? = null, val name: String) {

    fun scale(factor: Rational): Ingredient = Ingredient(
        quantity?.let { it * factor },
        unit,
        name
    )

    override fun toString(): String = listOfNotNull(quantity, unit, name)
        .joinToString(" ") { obj: Any -> obj.toString() }

    companion object {
        @JvmStatic
        // TODO
        fun parse(string: String): Ingredient {
            val units = Unit.regex()
            val regex = "^(?<quant>${Rational.REGEX})?\\s?((?<unit>$units)?\\s(?<name>.*?))?$"
            val p = Pattern.compile(regex)
            val m = p.matcher(string)
            return if (m.find()) {
                val quant = Optional.ofNullable(m.group("quant"))
                    .filter { r: String -> !r.isEmpty() }
                    .map { r: String -> Rational.parse(r.trim { it <= ' ' }) }
                    .orElse(null)
                val unit = m.group("unit")
                val name = m.group("name")
                Ingredient(quant, unit, name)
            } else {
                Ingredient(null, null, string)
            }
        }
    }
}