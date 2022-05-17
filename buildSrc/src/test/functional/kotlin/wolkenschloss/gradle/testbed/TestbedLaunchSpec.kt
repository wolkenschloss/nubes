package wolkenschloss.gradle.testbed

import com.jayway.jsonpath.JsonPath
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.gradle.testbed.domain.DomainOperations
import wolkenschloss.gradle.testbed.domain.DomainTasks.Companion.BUILD_DOMAIN_TASK_NAME
import wolkenschloss.testing.Template
import wolkenschloss.testing.build
import java.util.concurrent.TimeUnit

class TestbedLaunchSpec : FunSpec({
   xcontext("testbed project") {
       afterSpec {
           ProcessBuilder("multipass", "delete", "launch")
               .start()
               .waitFor()

           ProcessBuilder("multipass", "purge")
               .start()
               .waitFor()
       }

       autoClose(Template("testbed/launch")).withClone {

           test("should launch multipass instance") {
                val result = build(BUILD_DOMAIN_TASK_NAME, "--info")

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
               val process = ProcessBuilder(
                   DomainOperations.ipAddressCommandLine("launch"))
                   .start()

               process.inputStream.reader().use {
                   val json = it.readText()
                   val ipAddress = JsonPath.parse(json).read<List<String>>(DomainOperations.ipAddressJsonPath).single()

                   workingDirectory
                       .resolve("build/run/hosts")
                       .readText()
                       .shouldBe("$ipAddress launch.fixture.test dummy1.fixture.test dummy2.fixture.test\n")
               }

               process.waitFor(30, TimeUnit.SECONDS).shouldBe(true)
           }
       }
   }
})