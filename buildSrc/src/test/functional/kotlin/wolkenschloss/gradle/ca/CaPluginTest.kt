package wolkenschloss.gradle.ca

import io.kotest.assertions.assertSoftly
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.TaskOutcome
import wolkenschloss.testing.Fixtures
import wolkenschloss.testing.build
import java.security.cert.X509Certificate

class CaPluginTest : FunSpec({
    context("A project using com.github.wolkenschloss.ca gradle plugin") {
        val fixture = Fixtures("ca").clone(tempdir())

        test("should create self signed root ca") {
            val result = fixture.build("create")

            result.task(":create")!!.outcome shouldBe TaskOutcome.SUCCESS

            val certificate = fixture.resolve("build/ca/ca.crt")
                .readX509Certificate()

            assertSoftly(certificate) {
                basicConstraints shouldBeGreaterThan -1
                basicConstraints shouldBe Int.MAX_VALUE
                keyUsage[5] shouldBe true
                keyUsage[6] shouldBe true

                shouldBeIssuedBy("CN=Root CA,O=Wolkenschloss,C=DE")
                subjectX500Principal.name shouldBe "CN=Root CA,O=Wolkenschloss,C=DE"
            }

            val privateKey = fixture.resolve("build/ca/ca.key")
                .readPrivateKey()

            privateKey.algorithm shouldBe "RSA"
        }
    }
})

fun haveIssuer(issuer: String) = object : Matcher<X509Certificate> {
    override fun test(value: X509Certificate) = MatcherResult(
        value.issuerX500Principal.name == issuer,
        "Certificate issuer '${value.issuerX500Principal.name} does not equal '$issuer'",
        "Certificate should not be issued by '$issuer'"
    )
}

infix fun X509Certificate.shouldBeIssuedBy(issuer: String) = this should haveIssuer(issuer)

