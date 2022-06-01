package family.haschka.wolkenschloss.cookbook.recipe

import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.stream.Stream

data class Ingredient(val quantity: Rational? = null, val unit: String? = null, val name: String) {

    fun scale(factor: Rational): Ingredient {
        val scaledQuantity = Optional.ofNullable<Rational>(quantity)
            .map { q: Rational -> q.times(factor) }
            .orElse(null)
        return Ingredient(scaledQuantity, unit, name)
    }

    override fun toString(): String {
        return Stream.of<Any>(quantity, unit, name)
            .filter { obj: Any? -> Objects.nonNull(obj) }
            .map { obj: Any -> obj.toString() }
            .collect(Collectors.joining(" "))
    }

    companion object {
        @JvmStatic
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