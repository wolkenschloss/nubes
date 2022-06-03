package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class ServingsSpec : FunSpec({
    context("Valid servings") {
        withData(1, 2, 9, 10, 99, 100) {
            amount -> Servings(amount).amount shouldBe amount
        }
    }

    context("Invalid servings") {
        withData(0, 101) {
            amount -> shouldThrow<IllegalArgumentException> { Servings(amount) }
        }
    }

    test("Servings should be displayed as strings") {
        Servings(42).toString() shouldBe "42"
    }
})