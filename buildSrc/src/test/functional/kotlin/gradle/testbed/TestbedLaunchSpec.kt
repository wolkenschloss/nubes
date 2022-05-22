package family.haschka.wolkenschloss.gradle.testbed

import com.jayway.jsonpath.JsonPath
import family.haschka.wolkenschloss.gradle.testbed.domain.DomainOperations
import family.haschka.wolkenschloss.gradle.testbed.domain.DomainTasks.Companion.LAUNCH_INSTANCE_TASK_NAME
import family.haschka.wolkenschloss.testing.Template
import family.haschka.wolkenschloss.testing.build
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.gradle.testkit.runner.TaskOutcome
import java.util.concurrent.TimeUnit

class TestbedLaunchSpec : FunSpec({
    // defined in fixtures/test/launch/build.gradle.kts
    val instanceName = "launch"

    context("testbed project") {
        afterSpec {
            withContext(Dispatchers.IO) {
                ProcessBuilder("multipass", "delete", instanceName)
                    .start()
                    .waitFor()
                ProcessBuilder("multipass", "purge")
                    .start()
                    .waitFor()
            }
        }

        autoClose(Template("testbed/launch")).withClone {

            test("should launch multipass instance") {
                val result = build(LAUNCH_INSTANCE_TASK_NAME, "--info")

                workingDirectory
                    .resolve("build/config/cloud-init/user-data")
                    .shouldExist()

                workingDirectory
                    .resolve("build/run/hosts")
                    .shouldExist()

                result.tasks(TaskOutcome.SUCCESS)
                    .map { task -> task.path }
                    .shouldContainAll(":transform")
            }

            test("should create hosts file") {
                val process = withContext(Dispatchers.IO) {
                    ProcessBuilder(DomainOperations.ipAddressCommandLine(instanceName))
                        .start()
                }

                process.inputStream.reader().use {
                    val json = it.readText()
                    val ipAddress = JsonPath.parse(json).read<List<String>>(DomainOperations.ipAddressJsonPath).single()

                    workingDirectory
                        .resolve("build/run/hosts")
                        .readText()
                        .shouldBe("$ipAddress launch.fixture.test dummy1.fixture.test dummy2.fixture.test\n")
                }

                withContext(Dispatchers.IO) {
                    process.waitFor(30, TimeUnit.SECONDS)
                }.shouldBe(true)
            }
        }
    }
})