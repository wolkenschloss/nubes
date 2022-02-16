package wolkenschloss.conventions

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldBeADirectory
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build

class WebappTest : DescribeSpec({

    describe("project with vue application") {

        val fixture = autoClose(Fixtures("webapp"))

        describe("build task") {
            it("should create jar file") {
                fixture.withClone {
                    val result = build("build")

                    result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
                    result.task(":vue")!!.outcome shouldBe TaskOutcome.SUCCESS
                    target.resolve("build/libs/fixture-webapp.jar").shouldExist()
                }
            }
        }

        describe("check task") {
            it("should run unit and e2e tasks") {
                fixture.withClone {
                    val result = build("check")

                    result.tasks(TaskOutcome.SUCCESS)
                        .map { task -> task.path }
                        .shouldContainAll(":unit", ":e2e", ":check")
                }
            }
        }

        describe("vue task") {
            it("should build vue app") {
                fixture.withClone {
                    val result = build("vue")

                    result.task(":vue")!!.outcome shouldBe TaskOutcome.SUCCESS

                    assertSoftly(target.resolve("build/classes/java/main/META-INF/resources")) {
                        shouldBeADirectory()
                        shouldContainFile("index.html")
                        resolve("js").shouldBeADirectory()
                    }
                }
            }
        }

        describe("unit task") {
            it("should run unit tests") {
                fixture.withClone {
                    val result = build("unit")

                    result.task(":unit")!!.outcome shouldBe TaskOutcome.SUCCESS
                    target.resolve("build/reports/tests/unit").shouldContainFile("junit.xml")
                }
            }
        }

        describe("e2e task") {
            it("should run e2e tests") {
                fixture.withClone {
                    val result = build("e2e")

                    result.task(":e2e")!!.outcome shouldBe TaskOutcome.SUCCESS
                    target.resolve("build/reports/tests/e2e").shouldBeADirectory()
                }
            }
        }
    }
})
