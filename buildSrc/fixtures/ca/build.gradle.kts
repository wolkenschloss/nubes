import wolkenschloss.gradle.ca.CreateTask
import java.nio.file.Paths
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("com.github.wolkenschloss.ca")
}
tasks {
    val create by registering(CreateTask::class) {
        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.key
//        privateKey.set(project.layout.buildDirectory.file("ca/ca.key").map {Paths.get(it.asFile.path)})

        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.crt
//        certificate.set(project.layout.buildDirectory.file("ca/ca.crt").map {Paths.get(it.asFile.path)})
    }

    val createWithValidity by registering(CreateTask::class) {
        notBefore.set(ZonedDateTime.parse(System.getProperty("notBefore")))
        notAfter.set(ZonedDateTime.parse(System.getProperty("notAfter")))
    }

    val createInUserDefinedLocation by registering(CreateTask::class) {
        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.key
        privateKey.set(project.layout.buildDirectory.file("ca/ca.key").map {Paths.get(it.asFile.path)})

        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.crt
        certificate.set(project.layout.buildDirectory.file("ca/ca.crt").map {Paths.get(it.asFile.path)})
    }
}