package family.haschka.wolkenschloss.gradle.ca

import org.gradle.api.Plugin
import org.gradle.api.Project
import family.haschka.wolkenschloss.gradle.testbed.Directories
import java.time.ZoneOffset
import java.time.ZonedDateTime
import org.gradle.kotlin.dsl.*

class CaPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.tasks {
            val create = register(CREATE_TASK_NAME, TrustAnchor::class) {
                group = CERTIFICATION_AUTHORITY_GROUP
                notBefore.convention(today)
                notAfter.convention(expireIn())

                certificate.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve(CA_CERTIFICATE_FILE_NAME).toFile().absolutePath
                    )
                )

                privateKey.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve(CA_PRIVATE_KEY_FILE_NAME).toFile().absolutePath
                    )
                )

                subject.convention(TRUST_ANCHOR_DEFAULT_SUBJECT)
            }

            register(TRUSTSTORE_TASK_NAME, TrustStore::class) {
                group = CERTIFICATION_AUTHORITY_GROUP
                certificate.set(project.tasks.named(CREATE_TASK_NAME, TrustAnchor::class).flatMap { it.certificate })
                truststore.convention(
                    target.layout.projectDirectory.file(
                        Directories.certificateAuthorityHome.resolve("ca.jks").toFile().absolutePath
                    )
                )
            }

            withType(ServerCertificate::class) {
                caCertificate.convention(create.flatMap { it.certificate })
                caPrivateKey.convention(create.flatMap { it.privateKey })
                subjectAlternativeNames.convention(listOf(ServerCertificate.DnsName(name)))

                val certPath = this.baseDirectory.map { it.resolve("crt.pem") }
                certificate.convention(certPath.map {
                    target.layout.projectDirectory.file(
                        it.toAbsolutePath().toString()
                    )
                })

                val keyPath = this.baseDirectory.map { it.resolve("key.pem") }
                privateKey.convention(keyPath.map {
                    target.layout.projectDirectory.file(
                        it.toAbsolutePath().toString()
                    )
                })
                onlyIf { outputs.files.none { it.exists() } }
            }
        }
    }

    private fun expireIn(): ZonedDateTime {
        return today.plus(TrustAnchor.DEFAULT_VALIDITY_PERIOD)
    }

    private val today = ZonedDateTime.now(ZoneOffset.UTC)

    companion object {
        const val NAME = "wolkenschloss.gradle.ca"
        const val CERTIFICATION_AUTHORITY_GROUP = "ca"
        const val CREATE_TASK_NAME = "ca"
        const val TRUSTSTORE_TASK_NAME = "truststore"
        const val CA_CERTIFICATE_FILE_NAME = "ca.crt"
        const val CA_PRIVATE_KEY_FILE_NAME = "ca.key"
        const val TRUST_ANCHOR_DEFAULT_SUBJECT = "C=DE,O=Wolkenschloss,OU=Development,CN=Wolkenschloss Root CA"
    }
}
