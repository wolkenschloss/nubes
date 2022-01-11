package wolkenschloss

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class DockerBuildTask : DockerBaseTask() {

    @get:InputDirectory
    abstract val inputDir : DirectoryProperty

    @get:Input
    abstract val tags: SetProperty<String>

    @get:OutputFile
    abstract val image: RegularFileProperty

    @get:Input
    abstract val uid: Property<Long>

    @get:Input
    abstract val gid: Property<Long>

    @get:Input
    abstract val userName: Property<String>

    @TaskAction
    fun execute() {
        val base = inputDir.get().asFile.path
        logger.info("Using directory {}", base)

        val imageId = docker.buildImageCmd()
            .withBaseDirectory(inputDir.get().asFile)
            .withDockerfile(inputDir.file("Dockerfile").get().asFile)
            .withTags(tags.get())
            .withBuildArg("UID", uid.get().toString())
            .withBuildArg("GID", gid.get().toString())
            .withBuildArg("UNAME", userName.get())
            .start()
            .awaitImageId()

        logger.info("imageId {}", imageId)
        image.get().asFile.writeText(imageId)
    }
}