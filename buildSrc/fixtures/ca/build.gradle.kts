import family.haschka.wolkenschloss.gradle.ca.TrustAnchor
import java.time.ZonedDateTime
import java.util.Optional

plugins {
    id("family.haschka.wolkenschloss.ca")
}
tasks {

    val ca by existing(TrustAnchor::class) {
        Optional.ofNullable(System.getProperty("notBefore"))
            .map { ZonedDateTime.parse(it) }
            .ifPresent {
                notBefore.set(it)
            }

        Optional.ofNullable(System.getProperty("notAfter"))
            .map { ZonedDateTime.parse(it) }
            .ifPresent {
                notAfter.set(it)
            }
    }

    val createInUserDefinedLocation by registering(TrustAnchor::class) {
        privateKey.set(project.layout.buildDirectory.file("ca/ca.key"))
        certificate.set(project.layout.buildDirectory.file("ca/ca.crt"))
        notBefore.set(ZonedDateTime.now())
        notAfter.set(ZonedDateTime.now().plusYears(5))
        subject.set("CN=Test fixture Root CA,OU=createInUserDefinedLocation,O=Wolkenschloss,C=DE")
    }

    val customSubject by registering(TrustAnchor::class) {
        subject.set("CN=Test fixture Root CA,OU=customSubject,O=Wolkenschloss,C=DE")
    }
}
