package wolkenschloss.gradle.testbed

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Destroys
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

abstract class Destroy : DefaultTask() {

    @get:Destroys
    abstract val buildDir: DirectoryProperty

    @get:Internal
    abstract val domain: Property<String>

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:Inject
    abstract val providerFactory: ProviderFactory

    @TaskAction
    fun destroy() {

        project.exec {
            commandLine("multipass", "exec", domain.get(), "--", "/bin/bash", "-c",
            "\"which microk8s && microk8s stop\"")
            isIgnoreExitValue = true
        }
        project.exec {
            commandLine("multipass", "delete", domain.get())
            isIgnoreExitValue = true
        }

        project.exec {
            commandLine("multipass", "purge")
        }

        fileSystemOperations.delete { delete(buildDir) }
    }
}
