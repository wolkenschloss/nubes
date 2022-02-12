package wolkenschloss.gradle.testbed.transformation

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import javax.inject.Inject

@Suppress("CdiInjectionPointsInspection")
abstract class TransformationExtension @Inject constructor(private val layout: ProjectLayout) {
    fun initialize() {
        transformedConfigurationDirectory.convention(layout.buildDirectory.dir("config"))
        configurationDirectory.set(layout.projectDirectory.dir("config"))
    }

    abstract val transformedConfigurationDirectory: DirectoryProperty
    abstract val configurationDirectory: DirectoryProperty
}