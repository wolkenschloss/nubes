package family.haschka.wolkenschloss.gradle.ca

import org.bouncycastle.asn1.ASN1Encodable

class UnknownAsn1Primitive(val simpleName: ASN1Encodable) : Throwable()
