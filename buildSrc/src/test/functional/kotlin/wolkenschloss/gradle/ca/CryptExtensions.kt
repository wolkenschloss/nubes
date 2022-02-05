package wolkenschloss.gradle.ca

import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
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
        val pair = PEMParser(it).readObject() as PEMKeyPair
        JcaPEMKeyConverter().getPrivateKey(pair.privateKeyInfo)
    }
}