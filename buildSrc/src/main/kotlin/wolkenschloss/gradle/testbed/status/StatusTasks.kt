package wolkenschloss.gradle.testbed.status

import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import wolkenschloss.gradle.testbed.domain.BuildDomain
import wolkenschloss.gradle.testbed.domain.CopyKubeConfig
import wolkenschloss.gradle.testbed.domain.DomainExtension
import wolkenschloss.gradle.testbed.domain.DomainTasks
import wolkenschloss.gradle.testbed.download.DownloadDistribution
import wolkenschloss.gradle.testbed.download.DownloadTasks
import wolkenschloss.gradle.testbed.pool.PoolExtension

class StatusTasks(domain: DomainExtension, pool: PoolExtension) {
    val domain: DomainExtension
    val pool: PoolExtension

    init {
        this.domain = domain
        this.pool = pool
    }

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
                poolOperations.set(pool.poolOperations)
                domainOperations.set(domain.domainOperations)
                domainName.convention(domain.name)
                kubeConfigFile.convention(readKubeConfig.get().kubeConfigFile)
                this.knownHostsFile.convention(knownHostsFile)
                downloadDir.convention(distributionDir)
                baseImageFile.convention(downloadDistribution.map { d: DownloadDistribution -> d.baseImage.get() })
            }
    }

    companion object {
        const val STATUS_TASK_NAME = "status"
    }
}