package wolkenschloss;

import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.domain.BuildDomain;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.pool.PoolTasks;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasks;

public class DomainTasks {

    public static final String BUILD_DOMAIN_TASK_NAME = "buildDomain";
    public static final String READ_KUBE_CONFIG_TASK_NAME = "readKubeConfig";

    private static final String GROUP_NAME = "domain";

    public final DomainExtension domain;
    public final Provider<Integer> port;

    public DomainTasks(DomainExtension domain,  Provider<Integer> port) {

        this.domain = domain;
        this.port = port;
    }

    void register(TaskContainer tasks) {
        registerBuildDomainTask(tasks);
        registerReadKubeConfig(tasks);
    }

    public void registerReadKubeConfig(TaskContainer tasks) {
        var knownHostsFile = tasks.named(BUILD_DOMAIN_TASK_NAME, BuildDomain.class)
                .map(BuildDomain::getKnownHostsFile)
                .get();

        tasks.register(
                READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.getDomainName().convention(domain.getName());
                    task.getKubeConfigFile().convention(domain.getKubeConfigFile());
                    task.getKnownHostsFile().convention(knownHostsFile);
                    task.getDomainOperations().set(domain.getDomainOperations());
                });
    }

    public void registerBuildDomainTask(TaskContainer tasks) {

        var buildPool = tasks.findByName(PoolTasks.BUILD_POOL_TASK_NAME);

        var domainDescription = tasks
                .named(
                    TransformationTasks.TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME,
                    Transform.class)
                .map(Transform::getOutputFile)
                .get();

        tasks.register(
                BUILD_DOMAIN_TASK_NAME,
                BuildDomain.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.dependsOn(buildPool);
                    task.setDescription("Starts the libvirt domain and waits for the callback.");
                    task.getDomain().convention(domain.getName());
                    task.getPort().convention(port);
                    task.getXmlDescription().convention(domainDescription);
                    task.getKnownHostsFile().convention(domain.getKnownHostsFile());
                    task.getDomainOperations().set(domain.getDomainOperations());
                });
    }
}
