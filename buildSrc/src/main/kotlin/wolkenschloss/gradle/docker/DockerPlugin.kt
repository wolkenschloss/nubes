package wolkenschloss.gradle.docker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider

class DockerPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.tasks.withType(BuildImageTask::class.java).configureEach {
            imageId.convention(defaultImageIdFile)
            inputDir.convention(defaultInputDir)
            tags.convention(defaultTags)
            dockerService.set(DockerService.getInstance(target.gradle))
        }
    }

    companion object {
        const val NAME = "wolkenschloss.gradle.docker"
    }
}

private val BuildImageTask.defaultImageIdFile: Provider<RegularFile>
    get() = project.layout.buildDirectory.file(".docker/${project.name}/${name}")

private val BuildImageTask.defaultTags: Set<String>
    get() = setOf("${project.name}/$name:${project.version}", "${project.name}/$name:latest")

private val BuildImageTask.defaultInputDir: Directory
    get() = project.layout.projectDirectory.dir("docker/$name")