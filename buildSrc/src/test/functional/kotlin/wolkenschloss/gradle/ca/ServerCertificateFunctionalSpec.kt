package wolkenschloss.gradle.ca

import io.kotest.core.spec.style.FunSpec
import wolkenschloss.testing.Template
import io.kotest.engine.spec.tempdir
import wolkenschloss.testing.createRunner
import org.gradle.testkit.runner.TaskOutcome
import io.kotest.matchers.shouldBe
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.cert.*
import java.time.Instant
import java.util.*
import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.IsolationMode
import io.kotest.matchers.file.shouldExist
import java.nio.file.Files

class ServerCertificateFunctionalSpec : FunSpec({

    autoClose(Template("certificate")).withClone {
        context("ca gradle plugin applied to project") {
            val xdgDataHome = tempdir()
            val environment = mapOf("XDG_DATA_HOME" to xdgDataHome.absolutePath)

            xcontext("executing localhost task") {
                val result = createRunner()
                    .withArguments("localhost", "-i")
                    .withEnvironment(environment)
                    .build()

                test("should be successful") {
                    result.task(":localhost")!!.outcome shouldBe TaskOutcome.SUCCESS
                }

                context("creating certificate for localhost") {

                    val bcCert = CertificateWrapper.read(xdgDataHome.resolve("wolkenschloss/ca/localhost/crt.pem"))
                    val ca = CertificateWrapper.read(xdgDataHome.resolve("wolkenschloss/ca/ca.crt"))

                    test("should have valid subject alternative name") {
                        assertSoftly(bcCert) {
                            subjectAlternativeNames shouldBe listOf("DNS:localhost")
                            issuer shouldBe X500Name("CN=Root CA, O=Wolkenschloss, C=DE")
                            subject shouldBe X500Name("CN=localhost")
                            keyUsage.hasUsages(KeyUsage.digitalSignature or KeyUsage.keyEncipherment) shouldBe true
                            extendedKeyUsages shouldBe arrayOf(KeyPurposeId.id_kp_serverAuth)
                            authorityKeyIdentifier.keyIdentifier shouldBe ca.subjectKeyIdentifier
                            isValid() shouldBe true
                            certificateHolder.isValidOn(Date.from(Instant.now())) shouldBe true
                            validateChain(ca)
                        }
                    }
                }

                test("creates private key") {
                    val file = xdgDataHome.resolve("wolkenschloss/ca/localhost/key.pem")

                    assertSoftly(file.readPrivateKey()) {
                        format shouldBe "PKCS#8"
                        algorithm shouldBe "RSA"
                    }
                }
            }

            xcontext("executing example task") {
                val result = createRunner()
                    .withArguments("example", "-i")
                    .withEnvironment(environment)
                    .build()

                println(result.output)

                test("should be successful") {
                    result.task(":example")!!.outcome shouldBe TaskOutcome.SUCCESS
                }

                test("should be set in certificate") {
                    val bcCert = CertificateWrapper.read(xdgDataHome.resolve("wolkenschloss/ca/example.com+1/crt.pem"))
                    bcCert.subjectAlternativeNames shouldBe listOf("DNS:example.com", "IP Address:127.0.0.1")
                }
            }

            context("certificate already exists") {
                val cert = xdgDataHome.resolve("wolkenschloss/ca/localhost/crt.pem")
                Files.createDirectories(cert.parentFile.toPath())
                cert.createNewFile()

                val key = xdgDataHome.resolve("wolkenschloss/ca/localhost/key.pem")
                Files.createDirectories(key.parentFile.toPath())
                key.createNewFile()

                val result = createRunner()
                    .withArguments("localhost", "-i")
                    .withEnvironment(environment)
                    .build()

                println(result.output)
                test("skip execution") {
                    result.task(":localhost")!!.outcome shouldBe TaskOutcome.SKIPPED
                }
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
}






