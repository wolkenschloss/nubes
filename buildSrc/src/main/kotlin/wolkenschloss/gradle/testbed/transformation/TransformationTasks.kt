package wolkenschloss.gradle.testbed.transformation

import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import wolkenschloss.gradle.ca.CreateTask

class TransformationTasks(private val values: Map<String, Provider<*>>,private val  extension: TransformationExtension, val ca: TaskProvider<CreateTask>) {

    fun register(tasks: TaskContainer) {
        tasks.register("transform", Copy::class.java) {
            dependsOn(ca)
            from(extension.configurationDirectory)
            into(extension.transformedConfigurationDirectory)
            expand(values)
        }
    }

    companion object {
        const val TRANSFORM_FILES_NAME = "transform"
        const val USER_DATA_FILE_NAME = "cloud-init/user-data"
    }
}