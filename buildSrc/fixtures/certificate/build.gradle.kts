import wolkenschloss.gradle.ca.ServerCertificate
import org.bouncycastle.asn1.x509.GeneralName

plugins {
    id("com.github.wolkenschloss.ca")
}
tasks {

    // Create TLS certificate for localhost
    val localhost by registering(ServerCertificate::class) {

    }

    val example by registering(ServerCertificate::class) {
        subjectAlternativeNames.set(listOf(
            ServerCertificate.DnsName("example.com"),
            ServerCertificate.IpAddress("127.0.0.1")
        ))
    }
}
