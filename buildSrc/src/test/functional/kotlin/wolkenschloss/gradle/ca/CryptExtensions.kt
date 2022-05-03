package wolkenschloss.gradle.ca

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo
import java.io.File
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

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
