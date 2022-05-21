package family.haschka.wolkenschloss.gradle.ca

import org.bouncycastle.asn1.x509.GeneralName

class UnprocessedGeneralName(private val simpleName: GeneralName) : Throwable() {
    override val message: String
        get() = "Unknown tag ${simpleName.tagNo} for '${simpleName.name}'"
}
