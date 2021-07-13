package wolkenschloss;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.domain.BuildDomain;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasksRegistrar;

public class DomainTasks {

    public final DomainExtension domain;
    public final Provider<RegularFile> kubeConfig;
    public final Provider<Integer> port;

    public DomainTasks(DomainExtension domain, Provider<RegularFile> kubeConfig, Provider<Integer> port) {

        this.domain = domain;
        this.kubeConfig = kubeConfig;
        this.port = port;
    }

    public static void registerReadKubeConfig(TaskContainer tasks, DomainTasks domainTasks) {
        var knownHostsFile = tasks.named(Registrar.BUILD_DOMAIN_TASK_NAME, BuildDomain.class)
                .map(BuildDomain::getKnownHostsFile)
                .get();

        tasks.register(
                Registrar.READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> {
                    task.getDomainName().convention(domainTasks.domain.getName());
                    task.getKubeConfigFile().convention(domainTasks.kubeConfig);
                    task.getKnownHostsFile().convention(knownHostsFile);
                    task.getDomainOperations().set(domainTasks.domain.getDomainOperations());
                });
    }

    public void registerBuildDomainTask(TaskContainer tasks) {

        var buildPool = tasks.findByName(Registrar.BUILD_POOL_TASK_NAME);

        var domainDescription = tasks
                .named(
                    TransformationTasksRegistrar.TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME,
                    Transform.class)
                .map(Transform::getOutputFile)
                .get();

        tasks.register(
                Registrar.BUILD_DOMAIN_TASK_NAME,
                BuildDomain.class,
                task -> {
                    task.setGroup(Registrar.BUILD_GROUP_NAME);
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
