package wolkenschloss.gradle.testbed.domain

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import wolkenschloss.gradle.testbed.transformation.domainDescription
import wolkenschloss.gradle.testbed.pool.BuildPool

class DomainTasks(private val extension: DomainExtension, private val port1: Provider<Int>) {
    fun register(tasks: TaskContainer) {
        registerBuildDomainTask(tasks)
        registerReadKubeConfigTasks(tasks)
        registerStartTask(tasks)
        registerCreateDockerConfigTasks(tasks)
    }

    private fun registerBuildDomainTask(tasks: TaskContainer) {
        val buildPool by tasks.existing(BuildPool::class)
        val transform by tasks.existing(Copy::class)

        tasks.register(BUILD_DOMAIN_TASK_NAME,BuildDomain::class.java) {
            group = GROUP_NAME
            dependsOn(buildPool)
            description = "Starts the libvirt domain and waits for the callback."
            domain.convention(extension.name)
            port.convention(port1)
            xmlDescription.set(transform.domainDescription)
            knownHostsFile.convention(extension.knownHostsFile)
            hostsFile.convention(extension.hostsFile)
            domainSuffix.convention(extension.domainSuffix)
            hosts.convention(extension.hosts)
            domainOperations.set(extension.domainOperations)
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
            domainName.convention(extension.name)
            kubeConfigFile.convention(extension.kubeConfigFile)
            domainOperations.set(extension.domainOperations)
            dependsOn(buildDomain)
        }
    }

    private fun registerCreateDockerConfigTasks(tasks: TaskContainer) {
        tasks.register(
            CREATE_DOCKER_CONFIG_TASK_NAME,
            CreateDockerConfig::class.java
        ) {
            group = GROUP_NAME
            description = "Creates a Docker configuration file in order to be able to access the unsecured registration of the test bench."
            dockerConfigFile.convention(extension.dockerConfigFile)
            domainOperations.set(extension.domainOperations)
            dependsOn(tasks.named(READ_KUBE_CONFIG_TASK_NAME))
        }
    }

    companion object {
        const val BUILD_DOMAIN_TASK_NAME = "buildDomain"
        const val READ_KUBE_CONFIG_TASK_NAME = "readKubeConfig"
        const val CREATE_DOCKER_CONFIG_TASK_NAME = "createDockerConfig"
        private const val START_TASK_NAME = "start"
        private const val GROUP_NAME = "domain"

        private fun registerStartTask(tasks: TaskContainer) {
            tasks.register(START_TASK_NAME, DefaultTask::class.java) {
                group = GROUP_NAME
                description = "The all in one lifecycle start task. Have a cup of coffee."
                dependsOn(tasks.named(CREATE_DOCKER_CONFIG_TASK_NAME))
            }
        }
    }
}