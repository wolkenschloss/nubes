package wolkenschloss.gradle.testbed.status

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import wolkenschloss.gradle.testbed.domain.BuildDomain
import wolkenschloss.gradle.testbed.domain.CopyKubeConfig
import wolkenschloss.gradle.testbed.domain.DomainExtension
import wolkenschloss.gradle.testbed.domain.DomainTasks
import wolkenschloss.gradle.testbed.download.DownloadDistribution
import wolkenschloss.gradle.testbed.download.DownloadTasks
import wolkenschloss.gradle.testbed.pool.PoolExtension


class StatusTasks(
    private val domain: DomainExtension,
    private val pool: PoolExtension,
    private val registry: Provider<String>,
    private val certificate: Provider<RegularFile>,
    private val truststore: Provider<RegularFile>
) {

    fun register(tasks: TaskContainer) {
        val readKubeConfig: TaskProvider<CopyKubeConfig> =
            tasks.named(DomainTasks.READ_KUBE_CONFIG_TASK_NAME, CopyKubeConfig::class.java)
        val knownHostsFile = tasks.named(DomainTasks.BUILD_DOMAIN_TASK_NAME, BuildDomain::class.java)
            .map { obj: BuildDomain -> obj.knownHostsFile }
            .get()
        val downloadDistribution: TaskProvider<DownloadDistribution> = tasks.named(
            DownloadTasks.DOWNLOAD_DISTRIBUTION_TASK_NAME,
            DownloadDistribution::class.java
        )
        val distributionDir = downloadDistribution.map { d: DownloadDistribution -> d.distributionDir.get() }

        tasks.register(
            STATUS_TASK_NAME,
            Status::class.java) {
                description = "Performs tests to ensure the function of the test bench."
                domainName.convention(domain.name)
                poolName.convention(pool.name)
                kubeConfigFile.convention(readKubeConfig.get().kubeConfigFile)
                this.knownHostsFile.convention(knownHostsFile)
                downloadDir.convention(distributionDir)
                baseImageFile.convention(downloadDistribution.map { d: DownloadDistribution -> d.baseImage.get() })
                registry.convention(this@StatusTasks.registry)
                certificate.convention(this@StatusTasks.certificate)
                truststore.convention(this@StatusTasks.truststore)
            }
    }

    companion object {
        const val STATUS_TASK_NAME = "status"
    }
}