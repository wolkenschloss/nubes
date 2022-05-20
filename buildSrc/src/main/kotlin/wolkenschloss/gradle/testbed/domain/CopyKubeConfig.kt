package family.haschka.wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

abstract class CopyKubeConfig : DefaultTask() {
    @get:Input
    abstract val domain: Property<String>

    @get:OutputFile
    abstract val kubeConfigFile: RegularFileProperty

    @TaskAction
    fun read() {
        logger.quiet("Copy Kubeconfig")

        val result = execute {
            commandLine("multipass", "exec", domain.get(), "--", "microk8s", "config")
        }


        logger.quiet(result.verify().output)
        kubeConfigFile.get().asFile.writeText(result.output)
        logger.quiet("Done")
    }
}