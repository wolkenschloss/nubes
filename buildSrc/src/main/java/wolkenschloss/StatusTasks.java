package wolkenschloss;

import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.domain.BuildDomain;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.download.DownloadDistribution;
import wolkenschloss.download.DownloadTasks;
import wolkenschloss.pool.PoolExtension;
import wolkenschloss.status.Status;

public class StatusTasks {
    public final DomainExtension domain;
    public final PoolExtension pool;

    public StatusTasks(DomainExtension domain, PoolExtension pool) {
        this.domain = domain;
        this.pool = pool;
    }

    public void register(TaskContainer tasks) {

        var readKubeConfig = tasks.named(DomainTasks.READ_KUBE_CONFIG_TASK_NAME, CopyKubeConfig.class);

        var knownHostsFile = tasks.named(DomainTasks.BUILD_DOMAIN_TASK_NAME, BuildDomain.class)
                .map(BuildDomain::getKnownHostsFile)
                .get();

        var downloadDistribution = tasks.named(
                DownloadTasks.DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class);

        var distributionDir = downloadDistribution.map(d -> d.getDistributionDir().get());

        tasks.register(
                Registrar.STATUS_TASK_NAME,
                Status.class,
                task -> {
                    task.getPoolOperations().set(pool.getPoolOperations());
                    task.getDomainOperations().set(domain.getDomainOperations());
                    task.getDomainName().convention(domain.getName());
                    task.getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
                    task.getKnownHostsFile().convention(knownHostsFile);
                    task.getDownloadDir().convention(distributionDir);
                    task.getBaseImageFile().convention(downloadDistribution.map(d -> d.getBaseImage().get()));
                });
    }
}
