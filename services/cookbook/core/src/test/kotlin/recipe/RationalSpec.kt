package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class RationalSpec : FunSpec({
    context("Rational numbers should be equal based on their reduced value") {
        withData(
            mapOf(
                "2/4 = 1/2" to (Rational(2, 4) to Rational(1, 2)),
                "4/4 = 1" to (Rational(4, 4) to Rational(1)),
                "5/2 = 5/2" to (Rational(5, 2) to Rational(5, 2)),
                "7/3 = 14/6" to (Rational(7, 3) to Rational(14, 6))
            )
        ) { (first, second) ->
            first shouldBe second
        }
    }

    val data = Rational.fractions + mapOf(
        "2 \u00bd" to Rational(5, 2),
        "2 \u2153" to Rational(7, 3)
    )
    context("Fractional integers prints unicode characters") {
        withData(data) { rational ->
            rational.toString() shouldBe this.testCase.name.testName
        }
    }

    context("Rational numbers can be parsed from strings with fractional integers") {
        withData(data) { rational ->
            Rational.parse(this.testCase.name.testName) shouldBe rational
        }
    }

    context("Rational numbers can not be parsed from invalid strings") {
        withData(
            mapOf(
                "empty string" to "",
                "slash without numbers" to "/",
                "no number after slash" to "1/",
                "no number before slash" to "/2",
                "not a rational number at all" to "something",
                "floating point number" to "0.5",
                "floating point number before or after slash" to "0.7/0.8",
                "negative zero only" to "-0",
                "negative zero and fraction" to "-0 1/3"
            )
        ) { input ->
            shouldThrow<InvalidNumber> { Rational.parse(input) }
        }
    }

    context("Can not create invalid rational numbers") {
        withData(mapOf(
            "1/0" to { Rational(1, 0) },
            "-1/0" to { Rational(-1, 0) },
            "0/0" to { Rational(0, 0) }
        )) { fn: () -> Rational ->
            shouldThrow<InvalidNumber>(fn)
        }
    }
    data class Operands(val left: Rational, val right: Rational)

    context("Multiplication") {
        withData(
            nameFn = { "${it.first.left} / ${it.first.right} = ${it.second}" },
            Operands(Rational(1, 2), Rational(1, 2)) to Rational(1, 4),
            Operands(Rational(2, 1), Rational(3, 1)) to Rational(6, 1),
            Operands(Rational(1, 3), Rational(1, 5)) to Rational(1, 15)
        ) { (operands, result) ->
            operands.left * operands.right shouldBe result
        }
    }

    context("Difference") {
        withData(
            nameFn = { "${it.first.left} - ${it.first.right} = ${it.second}" },
            Operands(Rational(1, 2), Rational(1, 2)) to Rational(0, 1),
            Operands(Rational(2, 1), Rational(3, 1)) to Rational(-1, 1),
            Operands(Rational(1, 3), Rational(1, 5)) to Rational(2, 15)
        ) { (operands, result) ->
            operands.left - operands.right shouldBe result
        }
    }

    context("Addition") {
        withData(
            nameFn = { "${it.first.left} + ${it.first.right} = ${it.second}" },
            Operands(Rational(1, 2), Rational(1, 2)) to Rational(1, 1),
            Operands(Rational(2, 1), Rational(3, 1)) to Rational(5, 1),
            Operands(Rational(1, 3), Rational(1, 5)) to Rational(8, 15)
        ) { (operands, result) ->
            operands.left + operands.right shouldBe result
        }
    }
})
