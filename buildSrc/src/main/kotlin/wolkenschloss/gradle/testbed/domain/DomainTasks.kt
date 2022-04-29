package wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import wolkenschloss.gradle.testbed.transformation.userData

class DomainTasks(private val extension: DomainExtension) {
    fun register(tasks: TaskContainer) {
        registerBuildDomainTask(tasks)
        registerReadKubeConfigTasks(tasks)
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

    private fun registerBuildDomainTask(tasks: TaskContainer) {
        val transform by tasks.existing(Copy::class)

        tasks.register(BUILD_DOMAIN_TASK_NAME, BuildDomain::class.java) {
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

    private fun registerReadKubeConfigTasks(tasks: TaskContainer) {
        val buildDomain by tasks.existing(BuildDomain::class)

        tasks.register(
            READ_KUBE_CONFIG_TASK_NAME,
            CopyKubeConfig::class.java
        ) {
            this.group = GROUP_NAME

            description = "Copies the Kubernetes client configuration to the localhost for further use by kubectl."
            domain.convention(extension.name)
            kubeConfigFile.convention(extension.kubeConfigFile)
            dependsOn(buildDomain)
        }
    }

    companion object {
        const val TLS_SECRETS_TASK_NAME = "tlsSecrets"
        const val BUILD_DOMAIN_TASK_NAME = "buildDomain"
        const val READ_KUBE_CONFIG_TASK_NAME = "readKubeConfig"
        private const val START_TASK_NAME = "start"
        private const val GROUP_NAME = "domain"

        private fun registerStartTask(tasks: TaskContainer) {
            tasks.register(START_TASK_NAME, DefaultTask::class.java) {
                group = GROUP_NAME
                description = "The all in one lifecycle start task. Have a cup of coffee."
                dependsOn(tasks.named(READ_KUBE_CONFIG_TASK_NAME))
            }
        }
    }
}