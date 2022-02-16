package wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

abstract class PushImage : DefaultTask() {
    @get:Input
    abstract val images: ListProperty<String>

    @get:InputFile
    abstract val truststore: RegularFileProperty


    @get:Internal
    abstract val registry: Property<String>

    @TaskAction
    fun execute() {
        val service = RegistryService(registry.get())
        images.get().forEach { image ->
            service.uploadImage(image, truststore)
        }
    }
}
