package family.haschka.wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import family.haschka.wolkenschloss.gradle.testbed.transformation.userData

class DomainTasks(private val extension: DomainExtension) {
    fun register(tasks: TaskContainer) {
        registerLaunchTask(tasks)
        registerCopyKubeConfigTask(tasks)
        registerStartTask(tasks)
        registerTlsSecretsTasks(tasks)
    }

    private fun registerTlsSecretsTasks(tasks: TaskContainer) {
        tasks.register(TLS_SECRETS_TASK_NAME, TlsSecretsTask::class.java) {
            group = "other"
            description = "Prints all TLS secrets and their certificates"
            domainName.convention(extension.name)
        }
    }

    private fun registerLaunchTask(tasks: TaskContainer) {
        val transform by tasks.existing(Copy::class)

        tasks.register(LAUNCH_INSTANCE_TASK_NAME, Launch::class.java) {
            group = GROUP_NAME
            description = "Launch testbed instance."
            domain.convention(extension.name)
            hostsFile.convention(extension.hostsFile)
            domainSuffix.convention(extension.domainSuffix)
            hosts.convention(extension.hosts)
            userData.convention(transform.userData)
            disk.convention(extension.disk)
            mem.convention(extension.mem)
            cpus.convention(extension.cpus)
            image.convention(extension.image)
        }
    }

    private fun registerCopyKubeConfigTask(tasks: TaskContainer) {
        val launch by tasks.existing(Launch::class)

        tasks.register(
            COPY_KUBE_CONFIG_TASK_NAME,
            CopyKubeConfig::class.java
        ) {
            this.group = GROUP_NAME

            description = "Copies the Kubernetes client configuration to the localhost for further use by kubectl."
            domain.convention(extension.name)
            kubeConfigFile.convention(extension.kubeConfigFile)
            dependsOn(launch)
        }
    }

    companion object {
        const val TLS_SECRETS_TASK_NAME = "tlsSecrets"
        const val LAUNCH_INSTANCE_TASK_NAME = "launch"
        const val COPY_KUBE_CONFIG_TASK_NAME = "copyKubeConfig"
        private const val START_TASK_NAME = "start"
        private const val GROUP_NAME = "domain"

        private fun registerStartTask(tasks: TaskContainer) {
            tasks.register(START_TASK_NAME, DefaultTask::class.java) {
                group = GROUP_NAME
                description = "The all in one lifecycle start task. Have a cup of coffee."
                dependsOn(tasks.named(COPY_KUBE_CONFIG_TASK_NAME))
            }
        }
    }
}