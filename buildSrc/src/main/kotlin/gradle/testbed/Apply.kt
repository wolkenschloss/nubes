package family.haschka.wolkenschloss.gradle.testbed

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class Apply : DefaultTask() {

    @get:Input
    abstract val domain: Property<String>

    @get:Option(option = "overlay", description = "Overlay to apply", )
    @get:Input
    abstract val overlay: Property<String>


    private fun mount(source: String, target: String, block: () -> Unit) {

        project.exec {
            commandLine = listOf("multipass", "mount", source, "${domain.get()}:${target}")
        }

        try {
            block()
        } finally {
            project.exec {
                commandLine = listOf("multipass", "umount", "${domain.get()}:${target}")
            }
        }
    }

    private val multipass = domain.map { listOf("multipass", "exec", it, "--") }
    private val docker = multipass.map { it + listOf("docker") }
    private val kubectl = multipass.map { it + listOf("microk8s", "kubectl") }

    @TaskAction
    fun apply() {
        val overlayPath = "/home/ubuntu/nubes/overlays/${overlay.get()}"

        mount(project.rootProject.layout.projectDirectory.asFile.absolutePath, "/home/ubuntu/nubes") {
            project.exec {
                commandLine = docker.map {
                    it + listOf("build", "-t", "nubes/generators/db-secret-generator", "/home/ubuntu/nubes/kustomize/db-secret-generator")
                }.get()
            }

            project.exec {
                commandLine = multipass.map {
                  it + listOf("/bin/bash", "-c",
                      "microk8s kubectl kustomize --enable-alpha-plugins $overlayPath | microk8s kubectl apply -f -")
                }.get()
            }
        }
    }
}
