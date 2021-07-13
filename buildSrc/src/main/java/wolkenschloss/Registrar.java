package wolkenschloss;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.domain.DomainTasks;
import wolkenschloss.pool.BuildPool;
import wolkenschloss.pool.PoolExtension;
import wolkenschloss.pool.PoolTasks;
import wolkenschloss.transformation.TransformationTasks;
import wolkenschloss.download.DownloadTasks;
import wolkenschloss.download.BaseImageExtension;
import wolkenschloss.status.StatusTasks;

public class Registrar {
    public static final String BUILD_GROUP_NAME = "build";


    public static final String DESTROY_TASK_NAME = "destroy";

    private final Project project;
    private final TestbedExtension extension;

    public Registrar(Project project, TestbedExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    public void register() {
        TaskContainer tasks = project.getTasks();

        var transformationTasks = new TransformationTasks(tasks);
        var values = extension.asPropertyMap(project.getObjects());

        transformationTasks.register(extension.getTransformation());
        transformationTasks.setValues(values);

        BaseImageExtension baseImage = extension.getBaseImage();
        var downloadTasks = new DownloadTasks(tasks);
        downloadTasks.register(baseImage);

        PoolExtension pool = extension.getPool();
        PoolTasks poolTasks = new PoolTasks(pool);
        poolTasks.register(tasks);

        DomainExtension domain = extension.getDomain();
        var port = extension.getHost().getCallbackPort();

        DomainTasks domainTasks = new DomainTasks(domain, port);
        domainTasks.register(tasks);

        StatusTasks statusTasks = new StatusTasks(domain, pool);
        statusTasks.register(tasks);

        registerDestroyTask(tasks);
    }

    private void registerDestroyTask(TaskContainer tasks) {
        var poolRunFile = tasks.named(PoolTasks.BUILD_POOL_TASK_NAME, BuildPool.class)
                .map(t -> t.getPoolRunFile().get());

        tasks.register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getPoolOperations().set(extension.getPool().getPoolOperations());
                    task.getDomain().convention(extension.getDomain().getName());
                    task.getPoolRunFile().convention(poolRunFile);
                    task.getBuildDir().convention(project.getLayout().getBuildDirectory());
                    task.getDomainOperations().set(extension.getDomain().getDomainOperations());
                });
    }
}
