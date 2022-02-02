import wolkenschloss.gradle.docker.RunContainerTask

plugins {
    id("com.github.wolkenschloss.docker")
}

tasks {
    val cat by registering(RunContainerTask::class) {
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

//        command.addAll("cat", "/mnt/data/datafile")
    }
}
