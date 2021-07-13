package wolkenschloss;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.pool.BuildPool;
import wolkenschloss.pool.PoolExtension;
import wolkenschloss.pool.PoolTasks;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasks;
import wolkenschloss.download.DownloadTasks;
import wolkenschloss.download.BaseImageExtension;

public class Registrar {
    public static final String BUILD_GROUP_NAME = "build";

    public static final String STATUS_TASK_NAME = "status";
    public static final String DESTROY_TASK_NAME = "destroy";

    private final Project project;
    private final TestbedExtension extension;

    public Registrar(Project project, TestbedExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    public void register() {
        TaskContainer tasks = getProject().getTasks();

        var transformationTasks = new TransformationTasks(tasks);
        transformationTasks.register(extension.getTransformation());

        BaseImageExtension baseImage = getExtension().getBaseImage();
        var downloadTasks = new DownloadTasks(tasks);
        downloadTasks.register(baseImage);

        PoolExtension pool = getExtension().getPool();
        PoolTasks poolTasks = new PoolTasks(pool);
        poolTasks.register(tasks);

        DomainExtension domain = getExtension().getDomain();
        var port = getExtension().getHost().getCallbackPort();

        DomainTasks domainTasks = new DomainTasks(domain, port);
        domainTasks.register(tasks);

        StatusTasks statusTasks = new StatusTasks(domain, pool);

        statusTasks.registerStatusTask(tasks);

        getProject().getTasks().withType(Transform.class).configureEach(
                task -> task.getScope().convention(getExtension().asPropertyMap(getProject().getObjects())));

        registerDestroyTask(getProject().getTasks());
    }

    private void registerDestroyTask(TaskContainer tasks) {
        var poolRunFile = tasks.named(PoolTasks.BUILD_POOL_TASK_NAME, BuildPool.class)
                .map(t -> t.getPoolRunFile().get());

        tasks.register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getPoolOperations().set(getExtension().getPool().getPoolOperations());
                    task.getDomain().convention(getExtension().getDomain().getName());
                    task.getPoolRunFile().convention(poolRunFile);
                    task.getBuildDir().convention(getProject().getLayout().getBuildDirectory());
                    task.getDomainOperations().set(getExtension().getDomain().getDomainOperations());
                });
    }


    private Project getProject() {
        return project;
    }

    private TestbedExtension getExtension() {
        return extension;
    }
}
