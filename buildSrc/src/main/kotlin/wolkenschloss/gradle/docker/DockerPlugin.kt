package wolkenschloss.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

class DockerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.withType(BuildImageTask::class.java).configureEach {
            imageId.convention(defaultImageIdFile(project))
            inputDir.convention(project.layout.projectDirectory.dir("docker/$name"))
            tags.convention(
                setOf(
                    "${project.name}/$name:${project.version}",
                    "${project.name}/$name:latest"
                )
            )
        }
    }

    companion object {
        const val NAME = "wolkenschloss.gradle.docker"
    }
}

private fun BuildImageTask.defaultImageIdFile(project: Project): Provider<RegularFile> {
    return project.layout.buildDirectory.file(".docker/${project.name}/${name}")
}