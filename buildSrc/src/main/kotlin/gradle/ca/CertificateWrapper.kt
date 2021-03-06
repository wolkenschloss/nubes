package family.haschka.wolkenschloss.gradle.ca

import org.bouncycastle.asn1.ASN1IA5String
import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.io.StringReader
import java.security.cert.*
import java.time.Instant
import java.util.*

class CertificateWrapper(private val certificateHolder: X509CertificateHolder) {

    val issuer: X500Name
        get() = certificateHolder.issuer

    val subject: X500Name
        get() = certificateHolder.subject

    val notBefore: Date
        get() = certificateHolder.notBefore

    val notAfter: Date
        get() = certificateHolder.notAfter

    val keyUsage: KeyUsage
        get() = KeyUsage.fromExtensions(this.certificateHolder.extensions)

    val extendedKeyUsages: Array<KeyPurposeId>
        get() = ExtendedKeyUsage.fromExtensions(this.certificateHolder.extensions).usages

    val subjectKeyIdentifier: ByteArray
        get() = SubjectKeyIdentifier.fromExtensions(this.certificateHolder.extensions).keyIdentifier

    val authorityKeyIdentifier: AuthorityKeyIdentifier
        get() = AuthorityKeyIdentifier.fromExtensions(this.certificateHolder.extensions)

    val x509Certificate: X509Certificate
        get() = JcaX509CertificateConverter()
            .setProvider(BouncyCastleProvider())
            .getCertificate(certificateHolder)


    fun validateChain(trustAnchor: CertificateWrapper) {
        val certchain: List<X509Certificate> = listOf(this.x509Certificate)
        val certPath = CertificateFactory.getInstance("X.509", BouncyCastleProvider()).generateCertPath(certchain)
        val certPathValidator = CertPathValidator.getInstance("PKIX", BouncyCastleProvider())

        val trust = hashSetOf(TrustAnchor(trustAnchor.x509Certificate, null))
        val param = PKIXParameters(trust)
        param.isRevocationEnabled = false
        param.date = Date()

        certPathValidator.validate(certPath, param)
    }

    fun isValidNow(): Boolean {
        val now = Date.from(Instant.now())
        return certificateHolder.isValidOn(now)
    }

    val subjectAlternativeNames: List<String>
        get() {

            val names = GeneralNames.fromExtensions(
                this.certificateHolder.extensions,
                Extension.subjectAlternativeName
            ) ?: return emptyList()

            return names.names.map {

                val value = when (val prim = it.name.toASN1Primitive()) {
                    is ASN1IA5String -> prim.string
                    is DEROctetString -> prim.toIpv4Address()
                    else -> throw UnprocessedGeneralName(it)
                }

                "${it.tag()}:$value"
            }
        }

    private fun DEROctetString.toIpv4Address() =
        this.octets.joinToString(".") { o -> o.toString() }


    private fun GeneralName.tag(): String {
        return when (this.tagNo) {
            GeneralName.dNSName -> "DNS"
            GeneralName.directoryName -> "DN"
            GeneralName.ediPartyName -> "EDI party name"
            GeneralName.iPAddress -> "IP Address"
            GeneralName.otherName -> "Other name"
            GeneralName.registeredID -> "Registered ID"
            GeneralName.rfc822Name -> "RFC 822 Name"
            GeneralName.uniformResourceIdentifier -> "URI"
            GeneralName.x400Address -> "X400 Address"

            else -> {
                throw InvalidArgumentException(tagNo)
            }
        }
    }

    override fun toString(): String {
        return "Subject: $subject, SAN: ${subjectAlternativeNames.joinToString(", ")}"
    }

    companion object {
        fun parse(pemEncoding: String): CertificateWrapper {
            val parser = PEMParser(StringReader(pemEncoding))
            val certHolder: X509CertificateHolder = parser.readObject() as X509CertificateHolder
            return CertificateWrapper(certHolder)
        }

        fun read(f: File): CertificateWrapper {
            return parse(f.readText())
        }
    }
}



