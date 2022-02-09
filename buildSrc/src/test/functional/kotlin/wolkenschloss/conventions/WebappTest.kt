package wolkenschloss.conventions

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldBeADirectory
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build
import java.nio.file.Paths

class WebappTest : DescribeSpec({

    describe("project with vue application") {
        // It is not possible to use the temporary directories provided by
        // Kotest or Java for cloned fixtures.
        //
        // The node_modules directory contains executable files that are
        // required for the test. No files may be executed in proper temporary
        // directories. This leads to an error in the test.
        val fixture = Paths.get(System.getProperty("user.dir"), "build", "tmp", "fixture").toFile()

        beforeEach {
            Fixtures("webapp").clone(fixture)
            fixture.shouldExist()
        }

        afterEach {
            fixture.walkBottomUp().forEach {
                withClue("deleting ${it.path}") {
                    it.delete() shouldBe true
                }
            }
        }

        describe("build task") {
            it("should create jar file") {

                val result = fixture.build("build")

                result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
                result.task(":vue")!!.outcome shouldBe TaskOutcome.SUCCESS
                fixture.resolve("build/libs/fixture-webapp.jar").shouldExist()

            }
        }

        describe("check task") {
            it("should run unit and e2e tasks") {
                val result = fixture.build("check")

                result.tasks(TaskOutcome.SUCCESS)
                    .map { task -> task.path }
                    .shouldContainAll(":unit", ":e2e", ":check")
            }
        }

        describe("vue task") {
            it("should build vue app") {
                val result = fixture.build("vue")

                result.task(":vue")!!.outcome shouldBe TaskOutcome.SUCCESS

                assertSoftly(fixture.resolve("build/classes/java/main/META-INF/resources")) {
                    shouldBeADirectory()
                    shouldContainFile("index.html")
                    resolve("js").shouldBeADirectory()
                }
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