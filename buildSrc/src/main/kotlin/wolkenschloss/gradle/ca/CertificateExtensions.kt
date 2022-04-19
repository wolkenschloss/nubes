package wolkenschloss.gradle.ca

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.Extension
import org.bouncycastle.asn1.x509.GeneralName
import org.bouncycastle.asn1.x509.GeneralNames
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.openssl.PEMParser
import java.io.StringReader
import java.time.Instant
import java.util.*

fun parseCertificate(pemEncoding: String): X509CertificateHolder {
    val parser = PEMParser(StringReader(pemEncoding))
    val certHolder: X509CertificateHolder = parser.readObject() as X509CertificateHolder
    return certHolder
}

class CertificateWrapper(private val certificateHolder: X509CertificateHolder) {

    val issuer: X500Name
        get() = certificateHolder.issuer

    val subject: X500Name
        get() = certificateHolder.subject

    val notBefore: Date
        get() = certificateHolder.notBefore

    val notAfter: Date
        get() = certificateHolder.notAfter

    fun isValid(): Boolean {
        val now = Date.from(Instant.now())
        return !(now.before(certificateHolder.notBefore) || now.after(certificateHolder.notAfter))
    }

    fun subjectAlternativeNames(): List<String> {
        val names = GeneralNames.fromExtensions(
            this.certificateHolder.extensions,
            Extension.subjectAlternativeName
        )

        if (names == null) {
            return emptyList()
        }

        return names.names.map {
            "${it.tag()}=${it.name.toASN1Primitive()}"
        }
    }

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
        return "Subject: $subject, SAN: ${subjectAlternativeNames().joinToString(", ")}"
    }
    companion object {
        fun parse(pemEncoding: String): CertificateWrapper {
            val parser = PEMParser(StringReader(pemEncoding))
            val certHolder: X509CertificateHolder = parser.readObject() as X509CertificateHolder
            return CertificateWrapper(certHolder)
        }
    }
}



