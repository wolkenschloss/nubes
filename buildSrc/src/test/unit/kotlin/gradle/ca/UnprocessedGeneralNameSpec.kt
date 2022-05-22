package family.haschka.wolkenschloss.gradle.ca

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.bouncycastle.asn1.x509.GeneralName

class UnprocessedGeneralNameSpec : FunSpec({
   context("GeneralName mit tag uniformResourceIdentifier") {
       val name = GeneralName(GeneralName.uniformResourceIdentifier, "isbn:123456")
       test("UnprocessedGeneralName sollte Nachricht haben") {
           UnprocessedGeneralName(name).message shouldBe "Unknown tag 6 for 'isbn:123456'"
       }
   }
})