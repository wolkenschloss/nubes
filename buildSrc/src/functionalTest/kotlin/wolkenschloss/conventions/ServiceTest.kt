package wolkenschloss.conventions

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build

class ServiceTest : FunSpec({


    context("project with quarkus application") {
        val fixture = Fixtures("service").clone(tempdir())

        beforeEach { fixture.build("clean") }

        test("should build quarkus service"){
            val result = fixture.build("build")

            result.tasks(TaskOutcome.SUCCESS)
                .map { task -> task.path}
                .shouldContainAll(":test", ":integrationTest", ":build")
        }
    }
})

