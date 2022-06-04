package family.haschka.wolkenschloss.cookbook.recipe

import kotlin.math.absoluteValue

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
            if ((numerator * denominator) < 0) {
                builder.append('-')
            }
            val number: Int = numerator.absoluteValue / denominator.absoluteValue
            if (number != 0) {
                builder.append(number)
                builder.append(" ")
                val rest = this.absoluteValue().minus(Rational(number, 1))
                appendRational(builder, rest)
            } else {
                appendRational(builder, this.absoluteValue())
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

    fun absoluteValue(): Rational {
        return Rational(this.numerator.absoluteValue, this.denominator.absoluteValue)
    }

    companion object {
        val codes: Map<Rational, Char> = hashMapOf(
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

        val fractions: Map<String, Rational> =
            codes.entries.associate { (key, value) -> value.toString() to key }

        const val REGEX: String =
            "(?<sign>[+-](?!0))?((?<number>(0|[1-9]\\d*))[ ]?(?!/))?(((?<numerator>(0|[1-9]\\d*))([\\/](?<denominator>[1-9]\\d*))*)|(?<fraction>[½⅔¾⅘⅚⅞⅓⅗¼⅖⅝⅕⅙⅜⅐⅛⅑⅒]))?"
        private val pattern = Regex("^$REGEX$", RegexOption.MULTILINE)

        fun parse(input: String): Rational {
            pattern.find(input)?.let {

                val sign = it.groups["sign"]
                val number = it.groups["number"]
                val fraction = it.groups["fraction"]
                val numerator = it.groups["numerator"]
                val denominator = it.groups["denominator"]

                println("in echt: number: $number, numerator: $numerator, denominator: $denominator, fraction: $fraction")

                val signValue = if (sign != null) { if (sign.value == "-") {Rational(-1)} else {Rational(1)}} else {Rational(1)}

                return signValue * if (number != null) {
                     (Rational(number.value.toInt()) + if (fraction != null) {
                        fractions.getValue(fraction.value)
                    } else if (numerator != null) {
                        Rational(numerator.value.toInt(), denominator!!.value.toInt())
                    } else {
                        Rational(0)
                    })
                } else  if(fraction != null) {
                        fractions.getValue(fraction.value)
                } else if (numerator  != null) {
                        Rational(numerator.value.toInt(), denominator!!.value.toInt())
                } else {
                    throw InvalidNumber()
                }
            }

            throw InvalidNumber()
        }
    }
}