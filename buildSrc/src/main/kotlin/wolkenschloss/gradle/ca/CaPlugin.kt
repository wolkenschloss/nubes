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
            val create = register(CREATE_TASK_NAME, CreateTask::class) {
                group = CERTIFICATION_AUTHORITY_GROUP
                notBefore.convention(today)
                notAfter.convention(expireIn())
                certificate.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve(CA_CERTIFICATE_FILE_NAME).toFile().absolutePath))

                privateKey.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve(CA_PRIVATE_KEY_FILE_NAME).toFile().absolutePath))
            }

            register(TRUSTSTORE_TASK_NAME, TruststoreTask::class) {
                group = CERTIFICATION_AUTHORITY_GROUP
                certificate.set(project.tasks.named(CREATE_TASK_NAME, CreateTask::class).flatMap { it.certificate })
                truststore.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve("ca.jks").toFile().absolutePath))
            }

            withType(ServerCertificate::class) {
                caCertificate.convention(create.flatMap { it.certificate })
                caPrivateKey.convention(create.flatMap { it.privateKey })

                val certPpath = Directories.certificateAuthorityHome.resolve("$name.pem").toAbsolutePath()
                certificate.convention(target.layout.projectDirectory.file(certPpath.toFile().absolutePath))

                val keyPath = Directories.certificateAuthorityHome.resolve("$name-key.pem").toAbsolutePath()
                privateKey.convention(target.layout.projectDirectory.file(keyPath.toFile().absolutePath))
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
        const val CREATE_TASK_NAME = "ca"
        const val TRUSTSTORE_TASK_NAME = "truststore"
        const val CA_CERTIFICATE_FILE_NAME = "ca.crt"
        const val CA_PRIVATE_KEY_FILE_NAME = "ca.key"
    }
}
