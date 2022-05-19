package wolkenschloss.gradle.ca

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.extensions.system.withEnvironment
import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.file.shouldStartWithPath
import io.kotest.matchers.shouldBe
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.style.BCStyle
import org.gradle.api.tasks.StopExecutionException
import org.gradle.kotlin.dsl.*
import org.gradle.testfixtures.ProjectBuilder
import wolkenschloss.gradle.testbed.Directories
import java.time.Duration
import java.time.ZonedDateTime

class CreateTaskSpec : FunSpec({

    context("A project with create task") {
        withEnvironment(mapOf("XDG_DATA_HOME" to tempdir().path)) {

            val projectDir = tempdir()
            val project = ProjectBuilder.builder()
                .withProjectDir(projectDir)
                .withName(PROJECT_NAME)
                .build()

            project.pluginManager.apply(CaPlugin::class.java)

            test("should have a task named ${CaPlugin.CREATE_TASK_NAME}") {
                shouldNotThrowAny {
                    project.tasks.named(CaPlugin.CREATE_TASK_NAME, TrustAnchor::class)
                }
            }

            test("should have a task names ${CaPlugin.TRUSTSTORE_TASK_NAME}") {
                shouldNotThrowAny {
                    project.tasks.named(CaPlugin.TRUSTSTORE_TASK_NAME, TrustStore::class)
                }
            }

            test("certificate file defaults to \$XDG_DATA_HOME/wolkenschloss/ca/ca.crt") {
                val create = project.tasks.named(CaPlugin.CREATE_TASK_NAME, TrustAnchor::class.java)
                create.get().certificate.get().asFile shouldStartWithPath  Directories.certificateAuthorityHome.resolve("ca.crt")
            }

            test("private key file defaults to \$XDG_DATA_HOME/wolkenschloss/ca/ca.key") {
                val create = project.tasks.named(CaPlugin.CREATE_TASK_NAME, TrustAnchor::class.java)
                create.get().privateKey.get().asFile shouldStartWithPath  Directories.certificateAuthorityHome.resolve("ca.key")
            }

            test("The default for the start of validity is the current time") {
                val ca by project.tasks.existing(TrustAnchor::class)
                ca.get().notBefore.get().shouldBeWithin(Duration.ofSeconds(5), ZonedDateTime.now())
            }

            test("The default validity period is 5 years") {
                val ca by project.tasks.existing(TrustAnchor::class)
                ca.get().notAfter.get().shouldBeWithin(Duration.ofSeconds(5), ZonedDateTime.now().plusYears(10))
            }

            test("should have default subject") {
                val ca by project.tasks.existing(TrustAnchor::class)
                ca.get().subject.get() shouldBe CaPlugin.TRUST_ANCHOR_DEFAULT_SUBJECT
            }

            test("should stop execution if certificate already exists") {
                val certificate = tempfile()
                val create = project.tasks.create("crash", TrustAnchor::class.java)
                create.certificate.set(certificate)

                val exception = shouldThrow<StopExecutionException> {
                    create.execute()
                }

                exception.message shouldBe "Certificate already exists"
            }

            test("should stop execution if private key already exists") {

                val create by project.tasks.creating(TrustAnchor::class) {
                    notBefore.set(ZonedDateTime.now())
                    notAfter.set(ZonedDateTime.now().plusYears(5))
                    privateKey.set(tempfile())
                    certificate.set(projectDir.resolve("build/ca/certificate"))
                }

                val exception = shouldThrow<StopExecutionException> {
                    create.execute()
                }

                exception.message shouldBe "Private key already exists"
            }

            test("should customize subject") {
                val custom by project.tasks.registering(TrustAnchor::class) {
                    subject {
                        addRDN(BCStyle.CN, "Wolkenschloss Root CA")
                        addRDN(BCStyle.OU, "Development")
                        addRDN(BCStyle.O, "Wolkenschloss")
                        addRDN(BCStyle.C, "DE")
                    }
                }

                X500Name(custom.get().subject.get()) shouldBe X500Name(CaPlugin.TRUST_ANCHOR_DEFAULT_SUBJECT)
            }

            test("should customize subject with dsl") {
                val customDsl by project.tasks.registering(TrustAnchor::class) {
                    subject {
                        this[BCStyle.CN] = "Wolkenschloss Root CA"
                    }
                }

                customDsl.get().subject.get() shouldBe "CN=Wolkenschloss Root CA"
            }
        }
    }
}) {
    companion object {
        const val PROJECT_NAME = "ca"
    }
}

