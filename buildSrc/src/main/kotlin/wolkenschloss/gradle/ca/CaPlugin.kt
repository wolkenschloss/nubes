package wolkenschloss.gradle.ca

import org.gradle.api.Plugin
import org.gradle.api.Project
import wolkenschloss.gradle.testbed.Directories
import java.time.ZoneOffset
import java.time.ZonedDateTime

class CaPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks.withType(CreateTask::class.java).configureEach {
            notBefore.convention(today)
            notAfter.convention(expireIn())
            certificate.convention(
                target.layout.projectDirectory.file(
                Directories.certificateAuthorityHome.resolve("ca.crt").toFile().absolutePath))

            privateKey.convention(
                target.layout.projectDirectory.file(
                    Directories.certificateAuthorityHome.resolve("ca.key").toFile().absolutePath))
        }
    }

    private fun expireIn(): ZonedDateTime {
        return today.plus(CreateTask.DEFAULT_VALIDITY_PERIOD)
    }

    private val today = ZonedDateTime.now(ZoneOffset.UTC)

    companion object {
        const val NAME = "wolkenschloss.gradle.ca"
    }
}
