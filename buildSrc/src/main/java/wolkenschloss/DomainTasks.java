package wolkenschloss;

import org.gradle.api.DefaultTask;
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
    public static final String START_TASK_NAME = "start";

    private static final String GROUP_NAME = "domain";

    public final DomainExtension domain;
    public final Provider<Integer> port;

    public DomainTasks(DomainExtension domain,  Provider<Integer> port) {

        this.domain = domain;
        this.port = port;
    }

    public static void registerStartTask(TaskContainer tasks) {
        tasks.register(
                START_TASK_NAME,
                DefaultTask.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("The all in one lifecycle start task. Have a cup of coffee.");
                    task.dependsOn(tasks.named(READ_KUBE_CONFIG_TASK_NAME));
                });
    }

    void register(TaskContainer tasks) {
        registerBuildDomainTask(tasks);
        registerReadKubeConfig(tasks);
        registerStartTask(tasks);
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
                    task.setDescription("Copies the Kubernetes client configuration to the localhost for further use by kubectl.");
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
