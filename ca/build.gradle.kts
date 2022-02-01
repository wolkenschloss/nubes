import wolkenschloss.gradle.docker.RunContainerTask

plugins {
    id("com.github.wolkenschloss.docker")
}

tasks {
    @Suppress("UNUSED_VARIABLE")
    val create by registering(RunContainerTask::class) {
        command.addAll("echo", "XXXX Run CA:run XXXX")
        doNotTrackState("Only side effect")
    }
}