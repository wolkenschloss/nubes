package wolkenschloss.gradle.ca

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class CaPluginTest : FunSpec({
    context("A project using com.github.wolkenschloss.ca gradle plugin") {
        val fixture = Fixtures("ca").clone(tempdir())

        test("should create self signed root ca") {
            val result = fixture.build("rootCa")

            result.task(":rootCa")!!.outcome shouldBe TaskOutcome.SUCCESS
            println(result.output)

            fixture.resolve("build").walkBottomUp().forEach {
                println(it.absolutePath)
            }

            val certificate = fixture.resolve("build/ca/ca.crt").inputStream().use {
                CertificateFactory.getInstance("X.509")
                    .generateCertificate(it) as X509Certificate
            }

            with(certificate) {
                withClue("should be a Certificate Authority") {
                    basicConstraints shouldBeGreaterThan -1
                }

                withClue("should have unlimited certification path") {
                    basicConstraints shouldBe Int.MAX_VALUE
                }

                withClue("should be used to sign certificates") {
                    keyUsage[5] shouldBe true
                }

                withClue("should be used to sign crls") {
                    keyUsage[6] shouldBe  true
                }

                issuerX500Principal.name shouldBe "CN=Root CA,O=Wolkenschloss,C=DE"
                subjectX500Principal.name shouldBe "CN=Root CA,O=Wolkenschloss,C=DE"
            }
        }
    }
})