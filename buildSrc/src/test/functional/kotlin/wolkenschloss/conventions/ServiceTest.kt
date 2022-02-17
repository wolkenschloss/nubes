package wolkenschloss.conventions

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Template
import wolkenschloss.testing.build

class ServiceTest : FunSpec({

    context("project with quarkus application") {
        autoClose(Template("service")).withClone {
            beforeEach { build("clean") }

            test("should build quarkus service"){
                val result = build("build")

                result.tasks(TaskOutcome.SUCCESS)
                    .map { task -> task.path}
                    .shouldContainAll(":test", ":integrationTest", ":build")
            }
        }
    }
})

