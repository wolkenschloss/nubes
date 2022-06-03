package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import java.util.regex.Pattern

class UnitSpec : FunSpec({
    val all = Unit.values()
        .filter{ x -> x != Unit.FLUID_ONCE }
        .flatMap { value -> listOf(value.unit) + value.aliases }

    context("Unit can be matched by regular expression") {
        withData(all) { unit ->
            val regex = Pattern.compile("^${Unit.regex()}$")
            val matcher = regex.matcher(unit)
            matcher.matches() shouldBe true
        }
    }

    context("Units containing regular expressions can be matched") {
        withData("fluid ounce","fl", "fl.oz.", "oz.fl.", "fl oz", "oz fl", "fl.oz", "oz.fl") { unit ->
            val regex = Pattern.compile("^${Unit.regex()}$")
            val matcher = regex.matcher(unit)
            matcher.matches() shouldBe true
        }
    }
})