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

    public static void registerBuildDomainTask(TaskContainer tasks, DomainTasks domainTasks) {

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
                    task.getDomain().convention(domainTasks.domain.getName());
                    task.getPort().convention(domainTasks.port);
                    task.getXmlDescription().convention(domainDescription);
                    task.getKnownHostsFile().convention(domainTasks.domain.getKnownHostsFile());
                    task.getDomainOperations().set(domainTasks.domain.getDomainOperations());
                });
    }
}
