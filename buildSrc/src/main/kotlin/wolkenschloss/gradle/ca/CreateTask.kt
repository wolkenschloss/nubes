package wolkenschloss.gradle.ca

import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
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
import java.io.StringWriter
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.time.Period
import java.time.ZonedDateTime
import java.util.*

/**
 * Erzeugt ein selbst signiertes Wurzelzertifikat.
 *
 * Mit dem
 */
abstract class CreateTask : DefaultTask() {

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

    private val random: SecureRandom by lazy {
        val rnd = SecureRandom()
        rnd.setSeed(SecureRandom.getSeed(20))
        rnd
    }

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

            val keyPair = generateKeyPair()

            val contentSigner = JcaContentSignerBuilder(SIGNING_ALGORITHM)
                .build(keyPair.private)

            val id =ByteArray(20)
            random.nextBytes(id)

            val extendedKeyUsage = ExtendedKeyUsage(arrayOf(KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth))
            val keyUsage = KeyUsage(KeyUsage.keyCertSign or KeyUsage.digitalSignature)

            val holder = createCertificateBuilder(keyPair)
                .addExtension(Extension.basicConstraints, true, BasicConstraints(true).encoded)
                .addExtension(Extension.keyUsage, false, keyUsage.encoded)
                .addExtension(Extension.extendedKeyUsage, false, extendedKeyUsage.encoded)
                .build(contentSigner)

            val cert = JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider())
                .getCertificate(holder)

            privateKey.writePem(keyPair.private) {
                val permission = hashSetOf(PosixFilePermission.OWNER_READ)
                Files.setPosixFilePermissions(this.toPath(), permission)
            }

            certificate.writePem(cert) {
                val permission = hashSetOf(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ)
                Files.setPosixFilePermissions(this.toPath(), permission)
            }

        } catch (e: Exception) {
            throw GradleException("Cannot create certificate: ${e.message}", e)
        }
    }

    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KEYPAIR_GENERATOR_ALGORITHM)
        keyPairGenerator.initialize(2048, random)
        return keyPairGenerator.generateKeyPair()
    }

    private fun createCertificateBuilder(keyPair: KeyPair): X509v3CertificateBuilder {
        return JcaX509v3CertificateBuilder(
            subject,
            randomSerialNumber(),
            Date.from(notBefore.get().toInstant()),
            Date.from(notAfter.get().toInstant()),
            subject,
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

    private val subject: X500Name by lazy {
        X500NameBuilder(BCStyle.INSTANCE)
            .addRDN(BCStyle.C, "DE")
            .addRDN(BCStyle.O, "Wolkenschloss")
            .addRDN(BCStyle.CN, "Root CA")
            .build()
    }

    private fun RegularFileProperty.writePem(obj: Any, function: File.() -> Unit = {}) {
        val file = get().asFile
        file.parentFile.mkdirs()
        val stringWriter = StringWriter()
        JcaPEMWriter(stringWriter).use {
            it.writeObject(obj)
        }

        file.writeText(stringWriter.toString())
        function(file)
    }

    companion object {
        private const val KEYPAIR_GENERATOR_ALGORITHM = "RSA"

        private const val SIGNING_ALGORITHM = "SHA256WithRSA"

        val DEFAULT_VALIDITY_PERIOD: Period = Period.ofYears(5)
    }
}