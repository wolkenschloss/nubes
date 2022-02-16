package wolkenschloss.gradle.ca

import org.gradle.api.Plugin
import org.gradle.api.Project
import wolkenschloss.gradle.testbed.Directories
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.gradle.kotlin.dsl.*

class CaPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks {
            val ca by registering(CreateTask::class) {
                group = CERTIFICATION_AUTHORITY_GROUP
                notBefore.convention(today)
                notAfter.convention(expireIn())
                certificate.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve("ca.crt").toFile().absolutePath))

                privateKey.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve("ca.key").toFile().absolutePath))
            }

            val truststore by registering(TruststoreTask::class) {
                group = CERTIFICATION_AUTHORITY_GROUP
                certificate.set(ca.flatMap { it.certificate })
                truststore.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve("ca.jks").toFile().absolutePath))
            }
        }
    }

    private fun expireIn(): ZonedDateTime {
        return today.plus(CreateTask.DEFAULT_VALIDITY_PERIOD)
    }

    private val today = ZonedDateTime.now(ZoneOffset.UTC)

    companion object {
        const val NAME = "wolkenschloss.gradle.ca"
        const val CERTIFICATION_AUTHORITY_GROUP = "ca"
    }
}
