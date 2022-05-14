package wolkenschloss.gradle.ca

import org.bouncycastle.asn1.DEROctetString
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import wolkenschloss.gradle.testbed.Directories
import java.io.File
import java.io.StringWriter
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.*
import javax.security.auth.x500.X500Principal

abstract class ServerCertificate : DefaultTask() {

    @Internal
    val baseDirectory: Provider<Path> = subjectAlternativeNames.map {
        val suffix = if (it.size > 1) {"+${it.size - 1}"} else { "" }
        val name = it[0].second
        val directory = "$name$suffix"
        Directories.certificateAuthorityHome.resolve(directory)
    }

    @Internal
    val generalNames = subjectAlternativeNames.map { GeneralNames( it.map { GeneralName(it.first, it.second)}.toTypedArray()) }

    @get:Input
    abstract val subjectAlternativeNames: ListProperty<Pair<Int, String>>

    @get:InputFile
    @get:Incremental
    abstract val caCertificate: RegularFileProperty

    @get:InputFile
    @get:Incremental
    abstract val caPrivateKey: RegularFileProperty

    @get:OutputFile
    abstract val certificate: RegularFileProperty

    @get:OutputFile
    abstract val privateKey: RegularFileProperty

    @TaskAction
    fun create() {

        if (outputs.files.all { it.exists() }) {
            return
        }

        val keyPair = generateKeyPair()

        val gen = JcaPKCS8Generator(keyPair.private, null)
        privateKey.writePem(gen.generate()) {
            val permission = hashSetOf(PosixFilePermission.OWNER_READ)
            Files.setPosixFilePermissions(this.toPath(), permission)
        }

        val caCertificate = caCertificate.map { it.asFile.readX509Certificate() }

        val extUtils = JcaX509ExtensionUtils()

        val x = caCertificate.map {
            val builder = JcaX509v3CertificateBuilder(
                it.subjectX500Principal,
                BigInteger.valueOf(System.currentTimeMillis()).multiply(BigInteger.valueOf(10)),
                Date(System.currentTimeMillis() - 1000L * 5),
                Date(System.currentTimeMillis() + THIRTY_DAYS),
                X500Principal("CN=localhost"),
                keyPair.public
            )

            builder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(keyPair.public))
            builder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(it))
            builder.addExtension(Extension.basicConstraints, true, BasicConstraints(false))
            builder.addExtension(Extension.subjectAlternativeName, false, generalNames.get())
            builder.addExtension(Extension.keyUsage, true, KeyUsage(KeyUsage.digitalSignature or KeyUsage.keyEncipherment))
            builder.addExtension(Extension.extendedKeyUsage, false, ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth))

            val signer = JcaContentSignerBuilder("SHA256WithRSA").setProvider(BouncyCastleProvider())
            val caPrivateKey = caPrivateKey.map { it.asFile.readPrivateKey() }

            caPrivateKey.map {
                JcaX509CertificateConverter().setProvider(BouncyCastleProvider()).getCertificate(builder.build(signer.build(it)))
            }.get()
        }

        certificate.writePem(x.get()) {
            val permission = hashSetOf(PosixFilePermission.OWNER_READ, PosixFilePermission.GROUP_READ, PosixFilePermission.OTHERS_READ)
            Files.setPosixFilePermissions(this.toPath(), permission)
        }
    }

    // Duplicates
    private fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(ServerCertificate.KEYPAIR_GENERATOR_ALGORITHM)
        keyPairGenerator.initialize(3072, random)
        return keyPairGenerator.generateKeyPair()
    }

    private val random: SecureRandom by lazy {
        val rnd = SecureRandom()
        rnd.setSeed(SecureRandom.getSeed(20))
        rnd
    }

    fun File.readX509Certificate(): X509Certificate {
        return inputStream().use {
            CertificateFactory.getInstance("X.509")
                .generateCertificate(it) as X509Certificate
        }
    }

    fun File.readPrivateKey(): PrivateKey {
        return reader().use {
            val parser = PEMParser(it)
            val convert = JcaPEMKeyConverter()
            val keyInfo = PrivateKeyInfo.getInstance(parser.readObject())
            convert.getPrivateKey(keyInfo)
        }
    }

    private fun RegularFileProperty.writePem(obj: Any, function: File.() -> Unit = {}) {
        val file = get().asFile
        println("Write PEM to ${file.absolutePath}")
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
        private const val THIRTY_DAYS = 1000L * 60 * 60 * 24 * 30

        fun DnsName(name: String) = GeneralName.dNSName to name
        fun IpAddress(value: String) = GeneralName.iPAddress to value
    }
}