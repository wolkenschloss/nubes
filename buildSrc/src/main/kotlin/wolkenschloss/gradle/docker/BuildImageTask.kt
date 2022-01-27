package wolkenschloss.gradle.docker

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.getValue

abstract class BuildImageTask : DefaultTask() {

    @get:Input
    abstract val tags: SetProperty<String>

    @get:OutputFile
    abstract val imageId: RegularFileProperty

    @get:InputDirectory
    abstract val inputDir: DirectoryProperty

    @get:Internal
    abstract val dockerService: Property<DockerService>

    @TaskAction
    fun execute() {
        imageId.get().asFile.parentFile.mkdirs()

        val docker by dockerService
        val id = docker.client.buildImageCmd()
            .withBaseDirectory(inputDir.get().asFile)
            .withDockerfile(inputDir.file("Dockerfile").get().asFile)
            .withTags(tags.get())
            .start()
            .awaitImageId()

        imageId.get().asFile.writeText(id)
    }
}