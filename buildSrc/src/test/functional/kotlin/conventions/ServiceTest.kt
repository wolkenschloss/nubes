package family.haschka.wolkenschloss.conventions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import org.gradle.testkit.runner.TaskOutcome
import family.haschka.wolkenschloss.testing.Template
import family.haschka.wolkenschloss.testing.build

class ServiceTest : FunSpec({

    context("project with quarkus application") {
        autoClose(Template("service")).withClone {
            beforeEach { build("clean") }

            test("should run integrationTest") {
                val result = build("quarkusIntTest")

                result.tasks(TaskOutcome.SUCCESS)
                    .map { task -> task.path}
                    .shouldContainAll(":test", ":quarkusIntTest", ":quarkusBuild")
            }
        }
    }
})

