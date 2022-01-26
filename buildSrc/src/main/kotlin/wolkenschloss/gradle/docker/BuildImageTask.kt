package wolkenschloss.gradle.docker

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class BuildImageTask : DefaultTask() {

    @get:Input
    abstract val tags: SetProperty<String>

    @get:OutputFile
    abstract val imageId: RegularFileProperty

    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @TaskAction
    fun execute() {
        logger.quiet("My name is ${this.name}")
    }
}