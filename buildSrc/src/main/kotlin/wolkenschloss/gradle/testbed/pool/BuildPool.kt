package wolkenschloss.gradle.testbed.pool

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import java.util.*
import javax.inject.Inject

@CacheableTask
abstract class BuildPool : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFile
    abstract val poolDescriptionFile: Property<File>

    @get:Input
    abstract val poolName: Property<String>

    @get:OutputFile
    abstract val poolRunFile: RegularFileProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:Inject
    abstract val providerFactory: ProviderFactory

    @TaskAction
    fun exec() {
        val poolOperations = PoolOperations.getInstance(project.gradle).get()
        try {
            val runFile = poolRunFile.get().asFile
            val runFileContent = providerFactory.fileContents(poolRunFile)
            if (runFile.exists()) {
                val oldPoolUuid = runFileContent.asText.map { name: String? -> UUID.fromString(name) }
                    .get()
                poolOperations.destroy(oldPoolUuid)
                fileSystemOperations.delete { delete(poolRunFile) }
                logger.info("Pool destroyed")
            }
            if (poolOperations.exists(poolName.get())) {
                val message = String.format(
                    "Der Pool %1\$s existiert bereits, aber die Markierung-Datei %2\$s ist nicht vorhanden.%n" +
                            "Löschen Sie ggf. den Storage Pool mit dem Befehl 'virsh pool-destroy %1\$s && virsh " +
                            "pool-undefine %1\$s'",
                    poolName.get(),
                    runFile.path
                )
                throw GradleException(message)
            }
            val uuid = poolOperations.create(poolDescriptionFile)
            Files.writeString(runFile.toPath(), uuid.toString())
        } catch (e: Exception) {
            throw GradleScriptException("Can not define storage pool", e)
        }
    }
}