import wolkenschloss.gradle.ca.CreateTask
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.util.Optional

plugins {
    id("com.github.wolkenschloss.ca")
}
tasks {

    val ca by existing(CreateTask::class) {
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

    val createInUserDefinedLocation by registering(CreateTask::class) {
        privateKey.set(project.layout.buildDirectory.file("ca/ca.key"))
        certificate.set(project.layout.buildDirectory.file("ca/ca.crt"))
        notBefore.set(ZonedDateTime.now())
        notAfter.set(ZonedDateTime.now().plusYears(5))
    }
}
