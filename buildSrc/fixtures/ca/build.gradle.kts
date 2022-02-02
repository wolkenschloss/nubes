import wolkenschloss.gradle.ca.RootCaTask

plugins {
    id("com.github.wolkenschloss.ca")
}
tasks {
    val rootCa by registering(RootCaTask::class) {

    }
}