package family.haschka.wolkenschloss.gradle.ca

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.file.shouldBeReadable
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.file.shouldNotBeWriteable
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.KeyUsage
import org.gradle.testkit.runner.TaskOutcome
import family.haschka.wolkenschloss.testing.Template
import family.haschka.wolkenschloss.testing.createRunner
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class CaPluginTest : FunSpec({

    autoClose(Template("ca")).withClone {
        context("A project using com.github.wolkenschloss.ca gradle plugin") {
            val xdgDataHome = tempdir()
            val environment = mapOf("XDG_DATA_HOME" to xdgDataHome.absolutePath)

            context("executing ca task") {
                val result = createRunner()
                    .withArguments(CaPlugin.CREATE_TASK_NAME)
                    .withEnvironment(environment)
                    .build()

                test("should be successful") {
                    result.task(":${CaPlugin.CREATE_TASK_NAME}")!!.outcome shouldBe TaskOutcome.SUCCESS
                }

                test("should create self signed root certificate") {
                    assertSoftly(CertificateWrapper.read(xdgDataHome.resolve("wolkenschloss/ca/ca.crt"))) {
                        x509Certificate.basicConstraints shouldBeGreaterThan -1
                        x509Certificate.basicConstraints shouldBe Int.MAX_VALUE
                        keyUsage.hasUsages(KeyUsage.keyCertSign) shouldBe true
                        issuer shouldBe X500Name(CaPlugin.TRUST_ANCHOR_DEFAULT_SUBJECT)
                        subject shouldBe X500Name(CaPlugin.TRUST_ANCHOR_DEFAULT_SUBJECT)
                    }
                }

                test("should create read only certificate") {
                    assertSoftly(xdgDataHome.resolve("wolkenschloss/ca/ca.crt")) {
                        shouldBeReadable()
                        shouldNotBeWriteable()
                    }
                }

                test("should create readonly private key") {
                    assertSoftly(xdgDataHome.resolve("wolkenschloss/ca/ca.key")) {
                        shouldNotBeWriteable()
                        shouldBeReadable()
                        readPrivateKey().algorithm shouldBe "RSA"
                    }
                }
            }

            context("executing truststore task") {
                val result = createRunner()
                    .withArguments(CaPlugin.TRUSTSTORE_TASK_NAME)
                    .withEnvironment(environment)
                    .build()

                test("should execute successfully") {
                    result.task(":${CaPlugin.TRUSTSTORE_TASK_NAME}")!!.outcome shouldBe TaskOutcome.SUCCESS
                }

                test("should create truststore file") {
                    xdgDataHome.resolve("wolkenschloss/ca/ca.jks").shouldExist()
                }
            }

            test("should customize validity") {

                val start = ZonedDateTime.of(
                    LocalDate.of(2022, 2, 4),
                    LocalTime.MIDNIGHT,
                    ZoneOffset.UTC
                )

                val end = ZonedDateTime.of(
                    LocalDate.of(2027, 2, 4),
                    LocalTime.MIDNIGHT,
                    ZoneOffset.UTC
                )

                val result = createRunner()
                    .withArguments(CaPlugin.CREATE_TASK_NAME, "-DnotBefore=$start", "-DnotAfter=$end")
                    .withEnvironment(environment)
                    .build()

                result.task(":${CaPlugin.CREATE_TASK_NAME}")!!.outcome shouldBe TaskOutcome.SUCCESS

                val certificate = xdgDataHome.resolve("wolkenschloss/ca/ca.crt")
                    .readX509Certificate()

                assertSoftly(certificate) {
                    notBefore.toUtc() shouldBe start
                    notAfter.toUtc() shouldBe end
                }
            }

            test("should create output in user defined location") {
                val result = createRunner()
                    .withArguments("createInUserDefinedLocation")
                    .withEnvironment(environment)
                    .build()

                result.task(":createInUserDefinedLocation")!!.outcome shouldBe TaskOutcome.SUCCESS

                assertSoftly(workingDirectory.resolve("build/ca")) {
                    shouldContainFile("ca.crt")
                    shouldContainFile("ca.key")
                }
            }
        }
    }
}) {
    override fun isolationMode(): IsolationMode = IsolationMode.InstancePerLeaf
}


private fun Date.toUtc(): ZonedDateTime {
    return ZonedDateTime.ofInstant(this.toInstant(), ZoneOffset.UTC)
}

fun haveIssuer(issuer: String) = object : Matcher<X509Certificate> {
    override fun test(value: X509Certificate) = MatcherResult(
        value.issuerX500Principal.name == issuer,
        "Certificate issuer '${value.issuerX500Principal.name} does not equal '$issuer'",
        "Certificate should not be issued by '$issuer'"
    )
}

infix fun X509Certificate.shouldBeIssuedBy(issuer: String) = this should haveIssuer(issuer)

