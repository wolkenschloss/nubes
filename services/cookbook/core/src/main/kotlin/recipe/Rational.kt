package family.haschka.wolkenschloss.cookbook.recipe

import java.util.regex.Pattern

data class Rational(private var numerator: Int, private var denominator: Int) {

    constructor(value: Int) : this(value, 1)

    init {
        if (denominator == 0) {
            throw InvalidNumber()
        }

        val gcd = gcd(numerator, denominator)

        numerator /= gcd
        denominator /= gcd
    }

    private fun gcd(a: Int, b: Int): Int {
        return if (b == 0) a else gcd(b, a % b)
    }


    override fun toString(): String {
        val builder = StringBuilder()
        if (denominator == 1) {
            builder.append(numerator)
        } else {
            val number: Int = numerator / denominator
            if (number != 0) {
                builder.append(number)
                builder.append(" ")
                val rest = this.minus(Rational(number, 1))
                appendRational(builder, rest)
            } else {
                appendRational(builder, this)
            }
        }
        return builder.toString()
    }

    private fun appendRational(builder: StringBuilder, rest: Rational) {
        if (codes.containsKey(rest)) {
            builder.append(codes[rest])
        } else {
            builder.append(rest.numerator)
            builder.append("/")
            builder.append(rest.denominator)
        }
    }

    operator fun minus(rational: Rational): Rational {
        val d: Int = denominator * rational.denominator
        val a: Int = numerator * rational.denominator
        val b: Int = rational.numerator * denominator
        return Rational(a - b, d)
    }

    operator fun plus(rational: Rational): Rational {
        val d: Int = denominator * rational.denominator
        val a: Int = numerator * rational.denominator
        val b: Int = rational.numerator * denominator
        return Rational(a + b, d)
    }

    operator fun times(b: Rational): Rational {
        return Rational(numerator * b.numerator, denominator * b.denominator)
    }

    companion object {
        private val codes: Map<Rational, Char> = hashMapOf(
            Rational(1, 2) to '\u00bd',
            Rational(1, 3) to '\u2153',
            Rational(2, 3) to '\u2154',
            Rational(1, 4) to '\u00bc',
            Rational(3, 4) to '\u00be',
            Rational(1, 5) to '\u2155',
            Rational(2, 5) to '\u2156',
            Rational(3, 5) to '\u2157',
            Rational(4, 5) to '\u2158',
            Rational(1, 6) to '\u2159',
            Rational(5, 6) to '\u215a',
            Rational(1, 7) to '\u2150',
            Rational(1, 8) to '\u215b',
            Rational(3, 8) to '\u215c',
            Rational(5, 8) to '\u215d',
            Rational(7, 8) to '\u215e',
            Rational(1, 9) to '\u2151',
            Rational(1, 10) to '\u2152',
        )

        private val fractions: Map<String, Rational> =
            codes.entries.associate { (key, value) -> value.toString() to key }

        const val REGEX: String = "((?<number>-?(0|[1-9]\\d*)(?<!-0))[ ]?)?(((?<numerator>-?(0|[1-9]\\d*)(?<!-0))([/](?<denominator>[1-9]\\d*))*)|(?<fraction>[½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒]))?"
        private val pattern: Pattern = Pattern.compile("^$REGEX$")

        @JvmStatic
        fun parse(input: String): Rational {
            val m = pattern.matcher(input)
            if (m.find()) {
                val number = m.group("number")
                val fraction = m.group("fraction")
                val numerator = m.group("numerator")
                val denominator = m.group("denominator")

                if (number != null) {
                    return if (fraction != null) {
                        fractions.getValue(fraction) + Rational(number.toInt(), 1)
                    } else if (numerator != null) {
                        Rational(number.toInt()) + Rational(numerator.toInt(), denominator.toInt())
                    } else {
                        Rational(number.toInt())
                    }
                } else {
                    if (fraction != null) {
                        return fractions.getValue(fraction)
                    } else if (numerator != null) {
                        return Rational(numerator.toInt(), denominator.toInt())
                    }
                }
            }
            throw InvalidNumber()
        }
    }
}