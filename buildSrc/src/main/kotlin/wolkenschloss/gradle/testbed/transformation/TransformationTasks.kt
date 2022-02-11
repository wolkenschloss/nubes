package wolkenschloss.gradle.testbed.transformation

import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer

class TransformationTasks(private val values: Map<String, Provider<*>>,private val  extension: TransformationExtension) {

    fun register(tasks: TaskContainer) {
        tasks.register("transform", Copy::class.java) {
            from(extension.configurationDirectory)
            into(extension.transformedConfigurationDirectory)
            expand(values)
        }
    }

    companion object {
        const val TRANSFORM_FILES_NAME = "transform"
        const val NETWORK_CONFIG_FILE_NAME = "cloud-init/network-config"
        const val USER_DATA_FILE_NAME = "cloud-init/user-data"
        const val POOL_DESCRIPTION_FILE_NAME = "vm/pool.xml"
        const val DOMAIN_DESCRIPTION_FILE_NAME = "vm/domain.xml"
        private const val TEMPLATE_FILENAME_EXTENSION = "mustache"
        private fun templateFilename(filename: String): String {
            return String.format("%s.%s", filename, TEMPLATE_FILENAME_EXTENSION)
        }
    }
}