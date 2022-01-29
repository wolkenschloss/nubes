package wolkenschloss.conventions

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldBeADirectory
import io.kotest.matchers.file.shouldBeAFile
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build

class WebappTest : DescribeSpec({

    describe("project with vue application") {
        val fixture = Fixtures("webapp").clone(tempdir())
        afterEach { fixture.build("clean") }

        describe("build task") {
            it("should create jar far") {
                val result = fixture.build("build", "-i")
                result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
                result.task(":vue")!!.outcome shouldBe TaskOutcome.SUCCESS
                fixture.resolve("build/libs/webapp-example.jar").shouldExist()
            }
        }

        describe("check task") {
            it("should run unit and e2e tasks") {
                val result = fixture.build("check")
                result.task(":unit")!!.outcome shouldBe TaskOutcome.SUCCESS
                result.task(":e2e")!!.outcome shouldBe TaskOutcome.SUCCESS
                result.task(":check")!!.outcome shouldBe TaskOutcome.SUCCESS

                result.tasks(TaskOutcome.SUCCESS)
                    .map { task -> task.path }
                    .shouldContainAll(":unit", ":e2e", ":check")
            }
        }

        describe("vue task") {
            it("should build vue app") {
                val result = fixture.build("vue")

                result.task(":vue")!!.outcome shouldBe TaskOutcome.SUCCESS
                val resource = fixture.resolve("build/classes/java/main/META-INF/resources")
                resource.shouldBeADirectory()
                resource.resolve("index.html").shouldBeAFile()
                resource.resolve("js").shouldBeADirectory()
            }
        }

        describe("unit task") {
            it("should run unit tests") {
                val result = fixture.build("unit")

                result.task(":unit")!!.outcome shouldBe TaskOutcome.SUCCESS
                fixture.resolve("build/reports/tests/unit")
                    .shouldContainFile("junit.xml")
            }
        }

        describe("e2e task") {
            it("should run e2e tests") {
                val result = fixture.build("e2e")

                result.task(":e2e")!!.outcome shouldBe TaskOutcome.SUCCESS
                val reports = fixture.resolve("build/reports/tests/e2e")
                reports.shouldBeADirectory()
            }
        }

    }
})