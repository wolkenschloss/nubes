package wolkenschloss.gradle.ca

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.file.shouldBeReadable
import io.kotest.matchers.file.shouldContainFile
import io.kotest.matchers.file.shouldNotBeWriteable
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.createRunner
import java.security.cert.X509Certificate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*

class CaPluginTest : FunSpec({
    context("A project using com.github.wolkenschloss.ca gradle plugin") {
        val fixture = Fixtures("ca").clone(tempdir())

        test("should create self signed root ca") {
            val home = tempdir()
            val result = fixture.createRunner()
                .withArguments("create")
                .withEnvironment(mapOf("XDG_DATA_HOME" to home.path))
                .build()

            result.task(":create")!!.outcome shouldBe TaskOutcome.SUCCESS

            println(result.output)

            assertSoftly(home.resolve("wolkenschloss/ca/ca.crt").readX509Certificate()) {
                basicConstraints shouldBeGreaterThan -1
                basicConstraints shouldBe Int.MAX_VALUE
                keyUsage[5] shouldBe true
                keyUsage[6] shouldBe true
                shouldBeIssuedBy("CN=Root CA,O=Wolkenschloss,C=DE")
                subjectX500Principal.name shouldBe "CN=Root CA,O=Wolkenschloss,C=DE"
            }
        }

        test("should customize validity") {
            val start = ZonedDateTime.of(2022, 2, 4, 0, 0, 0, 0, ZoneOffset.UTC)
            val end = ZonedDateTime.of(2027, 2, 4, 0, 0, 0, 0, ZoneOffset.UTC)

            val home = tempdir()
            val result = fixture.createRunner()
                .withArguments("createWithValidity", "-DnotBefore=$start","-DnotAfter=$end")
                .withEnvironment(mapOf("XDG_DATA_HOME" to home.path))
                .build()

            result.task(":createWithValidity")!!.outcome shouldBe TaskOutcome.SUCCESS

            val certificate = home.resolve("wolkenschloss/ca/ca.crt")
                .readX509Certificate()

            assertSoftly(certificate) {
                notBefore.toUtc() shouldBe start
                notAfter.toUtc() shouldBe end
            }
        }

        test("should create readonly private key") {
            val data = tempdir()
            val result = fixture.createRunner()
                .withArguments("create")
                .withEnvironment(mapOf("XDG_DATA_HOME" to data.path))
                .build()

            println(result.output)
            result.task(":create")!!.outcome shouldBe TaskOutcome.SUCCESS

            assertSoftly(data.resolve("wolkenschloss/ca/ca.key")) {
                shouldNotBeWriteable()
                shouldBeReadable()
                readPrivateKey().algorithm shouldBe "RSA"
            }
        }

        test("should create output in user defined location") {
            val result = fixture.createRunner()
                .withArguments("createInUserDefinedLocation")
                .build()

            result.task(":createInUserDefinedLocation")!!.outcome shouldBe TaskOutcome.SUCCESS

            assertSoftly(fixture.resolve("build/ca")) {
                shouldContainFile("ca.crt")
                shouldContainFile("ca.key")
            }
        }
    }
})

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

