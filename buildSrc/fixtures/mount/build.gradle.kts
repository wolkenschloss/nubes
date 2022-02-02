import wolkenschloss.gradle.docker.RunContainerTask

plugins {
    id("com.github.wolkenschloss.docker")
}

tasks {
    val cat by registering(RunContainerTask::class) {
        command.addAll("echo", "hello world")
    }
}
