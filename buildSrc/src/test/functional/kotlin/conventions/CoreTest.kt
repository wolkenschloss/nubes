package family.haschka.wolkenschloss.conventions

import family.haschka.wolkenschloss.testing.Template
import family.haschka.wolkenschloss.testing.build
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome

class CoreTest : FunSpec({

    autoClose(Template("core")).withClone {

        context("project with core library") {

            beforeEach { build("clean") }

            test("Unit tests can use the JUnit 5 framework") {
                overlay(Template.resolve("overlay/core/test")) {
                    val result = build("test")
                    result.task(":test")!!.outcome shouldBe TaskOutcome.SUCCESS
                }
            }

            test("Source code can be written in Java with language level 11") {
                overlay(Template.resolve("overlay/core/java11")) {
                    val result = build("build")
                    result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
                }
            }

            test("Source code can be written in Java with language level 16") {
                overlay(Template.resolve("overlay/core/java16")) {
                    val result = build("build")
                    result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
                }
            }
        }
    }
})