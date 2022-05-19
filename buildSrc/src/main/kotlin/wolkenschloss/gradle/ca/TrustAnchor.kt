package wolkenschloss.gradle.ca

import org.bouncycastle.asn1.ASN1ObjectIdentifier
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.security.KeyPair
import java.time.Period
import java.time.ZonedDateTime
import java.util.*

/**
 * Erzeugt ein selbst signiertes Wurzelzertifikat.
 *
 * Mit dem
 */
abstract class TrustAnchor : DefaultTask() {

    @get:Input
    abstract val notBefore: Property<ZonedDateTime>

    @get:Input
    abstract val notAfter: Property<ZonedDateTime>

    /**
     * Dies ist die Datei, in die der private Schlüssel gespeichert wird.
     */
    @get:OutputFile
    abstract val privateKey: RegularFileProperty

    /**
     * Datei, in die das Zertifikat geschrieben wird.
     */
    @get:OutputFile
    abstract val certificate: RegularFileProperty

    // see https://stackoverflow.com/questions/29852290/self-signed-x509-certificate-with-bouncy-castle-in-java
    @TaskAction
    fun execute() {

        if (certificate.get().asFile.exists()) {
            throw StopExecutionException("Certificate already exists")
        }

        if (privateKey.get().asFile.exists()) {
            throw StopExecutionException("Private key already exists")
        }

        try {

            val keyPair = KeyPair()

            val contentSigner = JcaContentSignerBuilder(SIGNING_ALGORITHM)
                .build(keyPair.private)

            val id =ByteArray(20)
            random.nextBytes(id)

            val keyUsage = KeyUsage(KeyUsage.keyCertSign)
            val extUtils: JcaX509ExtensionUtils = JcaX509ExtensionUtils()
            val holder = createCertificateBuilder(keyPair)
                .addExtension(Extension.basicConstraints, true, BasicConstraints(true).encoded)
                .addExtension(Extension.keyUsage, false, keyUsage.encoded)
                .addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(keyPair.public))
                .build(contentSigner)

            val cert = JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider())
                .getCertificate(holder)

            // mkcert accepts only private keys in pkcs8 format
            val gen = JcaPKCS8Generator(keyPair.private, null)
            privateKey.get().asFile.writePem(gen.generate(), fun File.() {
                val permission = hashSetOf(PosixFilePermission.OWNER_READ)
                Files.setPosixFilePermissions(toPath(), permission)
            })

            certificate.get().asFile.writePem(cert, fun File.() {
                val permission = hashSetOf(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.OTHERS_READ
                )
                Files.setPosixFilePermissions(toPath(), permission)
            })

        } catch (e: Exception) {
            throw GradleException("Cannot create certificate: ${e.message}", e)
        }
    }

    private fun createCertificateBuilder(keyPair: KeyPair): X509v3CertificateBuilder {
        return JcaX509v3CertificateBuilder(
            X500Name(subject.get()),
            randomSerialNumber(),
            Date.from(notBefore.get().toInstant()),
            Date.from(notAfter.get().toInstant()),
            X500Name(subject.get()),
            keyPair.public
        )
    }

    private fun X509v3CertificateBuilder.addBasicConstraints(ca: Boolean): X509v3CertificateBuilder {
        return addExtension(Extension.basicConstraints, true, BasicConstraints(ca))
    }

    private fun X509v3CertificateBuilder.addKeyUsageExtension(usage: Int): X509v3CertificateBuilder {
        return addExtension(Extension.keyUsage, true, KeyUsage(usage))
    }

    private fun randomSerialNumber(): BigInteger {
        return BigInteger(160, random)
    }

    @get:Input
    abstract val subject: Property<String>

    fun subject( block: X500NameBuilder.() -> Unit ) {
        val builder = X500NameBuilder(BCStyle.INSTANCE)
        block(builder)
        val x509Name = builder.build()

        subject.set(project.provider { x509Name.toString() })
    }

    operator fun X500NameBuilder.set(index: ASN1ObjectIdentifier, value: String) {
        this.addRDN(index, value)
    }

    companion object {

        private const val SIGNING_ALGORITHM = "SHA256WithRSA"

        val DEFAULT_VALIDITY_PERIOD: Period = Period.ofYears(10)
    }
}
