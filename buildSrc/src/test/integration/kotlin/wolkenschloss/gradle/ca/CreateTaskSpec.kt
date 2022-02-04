package wolkenschloss.gradle.ca

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Paths
import java.time.Duration
import java.time.ZonedDateTime

class CreateTaskSpec : FunSpec({

    context("A project with create task") {
        withEnvironment(mapOf("XDG_DATA_HOME" to tempdir().path)) {
            val applicationHomeDir = Paths.get(
                System.getenv("XDG_DATA_HOME"),
                "wolkenschloss",
                "ca")

            val projectDir = tempdir()
            val project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName(PROJECT_NAME)
                .build()

            project.pluginManager.apply(CaPlugin::class.java)

            test("certificate file defaults to \$XDG_DATA_HOME/wolkenschloss/ca/ca.crt") {
                val create = project.tasks.create("create_cert", CreateTask::class.java)
                create.certificate.get() shouldBe applicationHomeDir.resolve("ca.crt")
            }

            test("private key file defaults to \$XDG_DATA_HOME/wolkenschloss/ca/ca.key") {
                val create = project.tasks.create("create_key", CreateTask::class.java)
                create.privateKey.get() shouldBe applicationHomeDir.resolve("ca.key")
            }
            test("The default for the start of validity is the current time") {
                val create = project.tasks.create("create_notBefore", CreateTask::class.java)
                create.notBefore.get().shouldBeWithin(Duration.ofSeconds(5), ZonedDateTime.now())
            }
            test("The default validity period is 5 years") {
                val create = project.tasks.create("create_notAfter", CreateTask::class.java)
                create.notAfter.get().shouldBeWithin(Duration.ofSeconds(5), ZonedDateTime.now().plusYears(5))
            }
        }
    }
}) {
    companion object {
        const val PROJECT_NAME = "ca"
    }
}