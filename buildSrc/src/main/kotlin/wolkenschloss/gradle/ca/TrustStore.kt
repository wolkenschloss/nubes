package family.haschka.wolkenschloss.gradle.ca

import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.openssl.PEMParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.StringReader
import java.security.KeyStore
import java.security.cert.X509Certificate

abstract class TrustStore : DefaultTask() {
    /**
     * Datei, in die das Zertifikat geschrieben wird.
     */
    @get:InputFile
    abstract val certificate: RegularFileProperty

    @get:OutputFile
    abstract val truststore: RegularFileProperty

    @TaskAction
    fun execute() {
        val c = certificate.map { it.asFile.readText() }.map { readCertificate(it) }.get()

        // Trust Store is readonly. The generated password is never used.
        val password = System.currentTimeMillis().toString(16)
        truststore.writeJks(c, password) {
        }
    }

    open fun readCertificate(pemEncoding: String): X509Certificate {
        val parser = PEMParser(StringReader(pemEncoding))
        val certHolder: X509CertificateHolder = parser.readObject() as X509CertificateHolder
        return JcaX509CertificateConverter().getCertificate(certHolder)
    }

    private fun RegularFileProperty.writeJks(
        cert: X509Certificate,
        password: String,
        function: File.() -> Unit = {}
    ) {

        val keyStore: KeyStore = KeyStore.getInstance("JKS")
        keyStore.load(null, null)
        keyStore.setCertificateEntry(ROOT_CA_ENTRY, cert)
        val bOut = ByteArrayOutputStream()
        keyStore.store(bOut, password.toCharArray())

        val file = get().asFile
        file.parentFile.mkdirs()

        file.writeBytes(bOut.toByteArray())
        function(file)
    }

    companion object {
        private const val ROOT_CA_ENTRY = "trustedca"
    }
}
