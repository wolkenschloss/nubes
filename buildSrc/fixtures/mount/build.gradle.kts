import wolkenschloss.gradle.docker.RunContainerTask

plugins {
    id("com.github.wolkenschloss.docker")
}

tasks {
    val catFile by registering(RunContainerTask::class) {
        mount {
            input {
                file {
                    source.set(project.layout.projectDirectory.file("volumes/datafile"))
                    target.set("/mnt/datafile")
                }
            }
        }

        command.addAll("cat", "/mnt/datafile")
    }

    val volume: String by project

    val catDir by registering(RunContainerTask::class) {
        mount {
            input {
                directory {
                    source.set(project.layout.projectDirectory.dir(volume))
                    target.set("/mnt/data")
                }
            }
        }

        command.addAll("cat", "/mnt/data/datafile")
    }

    val text: String by project

    val write by registering(RunContainerTask::class) {
        mount {
            output {
                source.set(project.layout.buildDirectory.dir("volumes/data"))
                target.set("/mnt/out")
            }
        }

        command.addAll("/bin/sh", "-c", "echo -n \"$text\" > /mnt/out/result && ls -lhaR /mnt")
    }
}
