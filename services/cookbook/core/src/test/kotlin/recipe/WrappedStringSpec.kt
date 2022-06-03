package family.haschka.wolkenschloss.cookbook.recipe

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMaxLength

class WrappedStringSpec : FunSpec({

    test("should wrap text") {
        WrappedString(
            """
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                voluptua. At vero eos et accusam et""".trimIndent())
            .wrap(30).split("\n").forEach {
            it shouldHaveMaxLength 30
        }
    }

    test("should not wrap if word is too long") {
        WrappedString("Loremipsumdolorsit amet,consetetursadipscingelitr, sed diam nonumy")
            .wrap(10) shouldBe """
                            Loremipsumdolorsit
                            amet,consetetursadipscingelitr,
                            sed diam
                            nonumy
        """.trimIndent()
    }

    test("should preserve markdown paragraphs") {
        WrappedString("""
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam
                voluptua. At vero eos et accusam et
                
                Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy
                eirmod tempor invidunt ut labore et dolore magna aliquyam erat.""".trimIndent())
            .wrap(40) shouldBe """
                Lorem ipsum dolor sit amet, consetetur
                sadipscing elitr, sed diam nonumy eirmod
                tempor invidunt ut labore et dolore
                magna aliquyam erat, sed diam voluptua.
                At vero eos et accusam et
                
                Lorem ipsum dolor sit amet, consetetur
                sadipscing elitr, sed diam nonumy eirmod
                tempor invidunt ut labore et dolore
                magna aliquyam erat.""".trimIndent()
    }
})