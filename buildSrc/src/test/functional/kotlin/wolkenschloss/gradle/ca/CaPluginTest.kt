package wolkenschloss.gradle.ca

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.file.shouldBeADirectory
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build

class CaPluginTest : FunSpec({
    context("A project using com.github.wolkenschloss.ca gradle plugin") {
        val fixture = Fixtures("ca").clone(tempdir())

        test("should create self signed root ca") {
            val result = fixture.build("rootCa")

            result.task(":rootCa")!!.outcome shouldBe TaskOutcome.SUCCESS
            fixture.resolve("build/ca").shouldBeADirectory()
        }
    }
})