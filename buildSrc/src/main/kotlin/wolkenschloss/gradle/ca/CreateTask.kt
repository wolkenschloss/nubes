package wolkenschloss.gradle.ca

import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x500.X500NameBuilder
import org.bouncycastle.asn1.x500.style.BCStyle
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.X509ExtensionUtils
import org.bouncycastle.cert.X509v3CertificateBuilder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.StringWriter
import java.math.BigInteger
import java.nio.file.Path
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.SecureRandom
import java.time.*
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
    abstract val privateKey: Property<Path>

    /**
     * Datei, in die das Zertifikat geschrieben wird.
     */
    @get:OutputFile
    abstract val certificate: Property<Path>

    private val random: SecureRandom by lazy {
        val rnd = SecureRandom()
        rnd.setSeed(SecureRandom.getSeed(20))
        rnd
    }

    @TaskAction
    fun execute() {
        try {
            logger.quiet("notBefore = ${notBefore.get()}")
            logger.quiet("notAfter = ${notAfter.get()}")
            logger.quiet("Certificate path ${certificate.get()}")
            logger.quiet("Private key path ${privateKey.get()}")
            logger.quiet("XDG_DATA_HOME ${System.getenv("XDG_DATA_HOME")}")
            val keyPair = generateKeyPair()

            val contentSigner = JcaContentSignerBuilder(SIGNING_ALGORITHM)
                .build(keyPair.private)

            val holder = createCertificateBuilder(keyPair)
                .addBasicConstraints(true)
                .addKeyUsageExtension(KeyUsage.keyCertSign or KeyUsage.digitalSignature or KeyUsage.cRLSign)
                .addSubjectKeyIdentifierExtension(keyPair.public)
                .build(contentSigner)

            val cert = JcaX509CertificateConverter()
                .setProvider(BouncyCastleProvider())
                .getCertificate(holder)

            privateKey.writePem(keyPair.private) {
                setWritable(false, false)
                setReadable(true, true)
            }

            certificate.writePem(cert)

        } catch (e: Exception) {
            println(e.message)
            throw GradleException("Das ging schief!", e)
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

    private fun X509v3CertificateBuilder.addSubjectKeyIdentifierExtension(key: PublicKey): X509v3CertificateBuilder {
        val algorithmIdentifier = AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1)
        val digestCalculator = BcDigestCalculatorProvider().get(algorithmIdentifier)
        val extensionUtils = X509ExtensionUtils(digestCalculator)
        val subjectPublicKey = SubjectPublicKeyInfo.getInstance(key.encoded)
        val subjectKeyIdentifier = extensionUtils.createSubjectKeyIdentifier(subjectPublicKey)

        return addExtension(Extension.subjectKeyIdentifier, true, subjectKeyIdentifier)
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

    private fun Provider<Path>.writePem(obj: Any, function: File.() -> Unit = {}) {
        val file = get().toFile()
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