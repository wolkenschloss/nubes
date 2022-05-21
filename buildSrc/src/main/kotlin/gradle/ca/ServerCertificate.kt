package family.haschka.wolkenschloss.gradle.ca

import org.bouncycastle.asn1.x509.*
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import org.gradle.work.Incremental
import family.haschka.wolkenschloss.gradle.testbed.Directories
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
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

        val keyPair = KeyPair()

        val gen = JcaPKCS8Generator(keyPair.private, null)
        privateKey.get().asFile.writePem(gen.generate(), fun File.() {
            val permission = hashSetOf(PosixFilePermission.OWNER_READ)
            Files.setPosixFilePermissions(toPath(), permission)
        })

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

        certificate.get().asFile.writePem(x.get(), fun File.() {
            val permission = hashSetOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.OTHERS_READ
            )
            Files.setPosixFilePermissions(toPath(), permission)
        })
    }

    companion object {
        private const val THIRTY_DAYS = 1000L * 60 * 60 * 24 * 30

        fun dnsName(name: String) = GeneralName.dNSName to name
        fun ipAddress(value: String) = GeneralName.iPAddress to value
    }
}