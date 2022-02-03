import wolkenschloss.gradle.ca.CreateTask


plugins {
    id("com.github.wolkenschloss.ca")
}
tasks {
    val rootCa by registering(CreateTask::class) {

        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.key
        privateKey.set(project.layout.buildDirectory.file("ca/ca.key"))

        // default is $XDG_DATA_HOME/wolkenschloss/ca/ca.crt
        certificate.set(project.layout.buildDirectory.file("ca/ca.crt"))
    }
}