import wolkenschloss.gradle.docker.BuildImageTask
import wolkenschloss.gradle.docker.RunContainerTask

plugins {
    id("com.github.wolkenschloss.docker")
}

tasks {
    val base by registering(BuildImageTask::class)

    // run container with 'base' image which contains hello.txt
    // see directory docker/base
    val cat by registering(RunContainerTask::class) {
        imageId.set(base.flatMap { it.imageId })
        command.addAll("cat", "hello.txt")
    }

    // uses default busybox image
    // echo Hello World, if -i option is set
    // No output if log level ist set to livecycle (default)
    val echo by registering(RunContainerTask::class) {
        command.addAll("echo", "Hello Echo")
    }

    // container output is not printed if task is called
    // with log level lower than info
    val silent by registering(RunContainerTask::class) {
        logging.captureStandardOutput(LogLevel.INFO)
        command.addAll("echo", "Hello Silence")
    }

    // write container stdout to file
    val log by registering(RunContainerTask::class) {
        logfile.set(project.layout.buildDirectory.file("log"))
        command.addAll("echo", "Hello Logfile")
    }
}
