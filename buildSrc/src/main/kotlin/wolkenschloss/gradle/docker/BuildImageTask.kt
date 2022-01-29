package wolkenschloss.gradle.docker

import com.github.dockerjava.api.model.Image
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
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

    @get:Input
    abstract val args: MapProperty<String, String>


    @TaskAction
    fun execute() {
        imageId.get().asFile.parentFile.mkdirs()
        logger.info("creating docker image: build context '${inputDir.get().asFile.absolutePath}'")

        val docker by dockerService
        val builder = docker.client.buildImageCmd()
            .withBaseDirectory(inputDir.get().asFile)
            .withDockerfile(inputDir.file("Dockerfile").get().asFile)
            .withTags(tags.get())

        args.get().forEach {entry -> builder.withBuildArg(entry.key, entry.value) }

        val id = builder.start().awaitImageId()

        imageId.get().asFile.writeText(id)
        logger.info("image $id created with tags ${tags.get().joinToString(", ")}")
    }
}

val Image.shortId: String
    get() = id.removePrefix("sha256:").take(12)