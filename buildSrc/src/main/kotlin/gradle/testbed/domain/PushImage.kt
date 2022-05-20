package family.haschka.wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class PushImage : DefaultTask() {
    @get:Input
    abstract val images: MapProperty<String, String>

    @get:InputFile
    abstract val truststore: RegularFileProperty


    @get:Internal
    abstract val registry: Property<String>

    @TaskAction
    fun execute() {
        val service = RegistryService(registry.get(), truststore)
        images.get().forEach { (src, dst) ->
            logger.info("push image $src -> $dst")
            service.push(src, dst)
        }
    }
}
