package wolkenschloss.gradle.testbed.transformation

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout

abstract class TransformationExtension {
    fun initialize(layout: ProjectLayout) {
        generatedConfigurationDirectory.convention(layout.buildDirectory.dir("config"))
        sourceDirectory.set(layout.projectDirectory.dir("config"))
    }

    abstract val generatedConfigurationDirectory: DirectoryProperty
    abstract val sourceDirectory: DirectoryProperty
}