package wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.FileWriter
import java.io.IOException
import java.lang.Exception

@Deprecated("Use secure registry")
abstract class CreateDockerConfig : DefaultTask() {

    @get:Input
    abstract val domain: Property<String>

    @get:OutputFile
    abstract val dockerConfigFile: RegularFileProperty

    @TaskAction
    fun write() {
        val domainOperations = DomainOperations.getInstance(project.gradle).get()
        val registryService: RegistryService = domainOperations.registry(domain)
        val registry = registryService.address
        val configFile = dockerConfigFile.get().asFile
        if (dockerConfigFile.get().asFile.parentFile.mkdirs()) {
            project.logger.info("Target directory created.")
        }
        if (configFile.exists()) {
            if (configFile.delete()) {
                project.logger.info("Old Docker configuration file deleted")
            }
        }
        try {
            if (configFile.createNewFile()) {
                project.logger.info("Docker configuration file created")
            }
        } catch (e: IOException) {
            throw GradleException("Can not create docker configuration file", e)
        }
        try {
            FileWriter(configFile).use { writer ->
                writer.append("{\n")
                    .append("  \"insecure-registries\" : [\"").append(registry).append("\"]\n")
                    .append("}\n")
                writer.flush()
            }
        } catch (exception: Exception) {
            throw GradleException("Can not write docker configuration file", exception)
        }
    }
}