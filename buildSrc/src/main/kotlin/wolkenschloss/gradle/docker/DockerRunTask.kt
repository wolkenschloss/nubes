package wolkenschloss.gradle.docker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class DockerRunTask : DefaultTask() {

    @TaskAction
    fun execute() {
    }
}
