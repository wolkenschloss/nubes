import wolkenschloss.gradle.ca.CreateTask
import java.nio.file.Paths
import java.time.ZonedDateTime

plugins {
    id("com.github.wolkenschloss.ca")
}
tasks {
    // The preset values are sufficient to perform the task
    val create by registering(CreateTask::class)

    // The validity period of the certificate can be overwritten
    val createWithValidity by registering(CreateTask::class) {
        notBefore.set(ZonedDateTime.parse(System.getProperty("notBefore")))
        notAfter.set(ZonedDateTime.parse(System.getProperty("notAfter")))
    }

    // The certificate and private key output files can be customized
    val createInUserDefinedLocation by registering(CreateTask::class) {
        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.key
        privateKey.set(project.layout.buildDirectory.file("ca/ca.key"))

        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.crt
        certificate.set(project.layout.buildDirectory.file("ca/ca.crt"))
    }
}
