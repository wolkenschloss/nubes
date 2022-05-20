import family.haschka.wolkenschloss.gradle.ca.ServerCertificate

plugins {
    id("family.haschka.wolkenschloss.ca")
}
tasks {

    register<ServerCertificate>("localhost") {
        description = "Create TLS certificate for localhost"
    }

    register<ServerCertificate>("example") {
        description = "Create TLS certificate for 'example.com' with IP Address 127.0.0.1"
        subjectAlternativeNames.set(listOf(
            ServerCertificate.DnsName("example.com"),
            ServerCertificate.IpAddress("127.0.0.1")
        ))
    }
}
