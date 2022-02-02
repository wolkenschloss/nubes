package wolkenschloss.gradle.ca

import org.gradle.api.Plugin
import org.gradle.api.Project

class CaPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.withType(RootCaTask::class.java).configureEach {
            outputDir.convention(project.layout.buildDirectory.dir("ca"))
        }
    }

    companion object {
        const val NAME = "wolkenschloss.gradle.ca"
    }
}
