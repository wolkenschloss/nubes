package wolkenschloss.gradle.testbed.status

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import wolkenschloss.gradle.testbed.domain.CopyKubeConfig
import wolkenschloss.gradle.testbed.domain.DomainExtension
import wolkenschloss.gradle.testbed.domain.DomainTasks


class StatusTasks(
    private val domain: DomainExtension,
    private val registry: Provider<String>,
    private val certificate: Provider<RegularFile>,
    private val truststore: Provider<RegularFile>
) {

    fun register(tasks: TaskContainer) {
        val readKubeConfig: TaskProvider<CopyKubeConfig> =
            tasks.named(DomainTasks.READ_KUBE_CONFIG_TASK_NAME, CopyKubeConfig::class.java)

        tasks.register(
            STATUS_TASK_NAME,
            Status::class.java) {
                description = "Performs tests to ensure the function of the test bench."
                domainName.convention(domain.name)
                kubeConfigFile.convention(readKubeConfig.get().kubeConfigFile)
                registry.convention(this@StatusTasks.registry)
                certificate.convention(this@StatusTasks.certificate)
                truststore.convention(this@StatusTasks.truststore)
            }
    }

    companion object {
        const val STATUS_TASK_NAME = "status"
    }
}