package wolkenschloss.conventions

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build
import wolkenschloss.testing.properties


class CoreTest : FunSpec({

    context("project with core library") {
        val fixture = Fixtures("core").clone(tempdir())

        beforeEach { fixture.build("clean") }

        test("Unit tests can use the JUnit 5 framework") {
            Fixtures("overlay/core/test").useOverlay(fixture) {
                val result = fixture.build("test")
                result.task(":test")!!.outcome shouldBe TaskOutcome.SUCCESS
            }
        }

        test("Source code can be written in Java with language level 11") {
            Fixtures("overlay/core/java11").useOverlay(fixture) {
                val result = fixture.build("build")
                result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
            }
        }
        test("Source code can be written in Java with language level 16") {
            Fixtures("overlay/core/java16").useOverlay(fixture) {
                val result = fixture.build("build")
                result.task(":build")!!.outcome shouldBe TaskOutcome.SUCCESS
            }
        }

        test("build should write version into project.properties file") {

            val result = fixture.build("classes", "--project-prop", "version=v1.0")

            result.task(":projectProperties")!!.outcome shouldBe TaskOutcome.SUCCESS

            fixture.properties(PROJECT_PROPERTIES_PATH)
                .shouldContain("project.version" , "v1.0")
        }

        test("build should process vcs information") {
            val result = fixture.build(
                "build",
                "--project-prop", "vcs.commit=$COMMIT_ID",
                "--project-prop", "vcs.ref=$REF",
                "--project-prop", "version=123"
            )

            result.task(":projectProperties")!!.outcome shouldBe TaskOutcome.SUCCESS

            fixture.properties(PROJECT_PROPERTIES_PATH).shouldContainAll(
                hashMapOf<Any, Any>(
                    "vcs.commit" to COMMIT_ID,
                    "vcs.ref" to REF,
                    "project.version" to "123",
                    "project.group" to "family.haschka.wolkenschloss.conventions",
                    "project.name" to "core"
                )
            )
        }
    }
}) {
    companion object {
        private const val PROJECT_PROPERTIES_PATH = "build/resources/main/project.properties"
        @Suppress("SpellCheckingInspection")
        private const val COMMIT_ID = "ffac537e6cbbf934b08745a378932722df287a53"
        private const val REF = "refs/heads/feature-branch-1"
    }
}




