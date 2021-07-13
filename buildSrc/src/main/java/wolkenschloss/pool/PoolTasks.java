package wolkenschloss.pool;

import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.Registrar;
import wolkenschloss.download.DownloadDistribution;
import wolkenschloss.download.DownloadTasksRegistrar;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasks;

public class PoolTasks {
    public static final String BUILD_POOL_TASK_NAME = "buildPool";
    public static final String BUILD_ROOT_IMAGE_TASK_NAME = "buildRootImage";
    public static final String BUILD_DATA_SOURCE_IMAGE_TASK_NAME = "buildDataSourceImage";

    private static final String GROUP_NAME = "pool";

    private final PoolExtension pool;

    public PoolTasks(PoolExtension pool) {
        this.pool = pool;
    }

    public void register(TaskContainer tasks) {
        registerBuildDataSourceImageTask(tasks, GROUP_NAME);
        registerBuildRootImageTask(tasks);
        registerBuildPoolTask(tasks);
    }

    public void registerBuildPoolTask(TaskContainer tasks) {

        var buildDataSourceImage = tasks.findByName(PoolTasks.BUILD_DATA_SOURCE_IMAGE_TASK_NAME);
        var buildRootImage = tasks.findByName(PoolTasks.BUILD_ROOT_IMAGE_TASK_NAME);

        var transformPoolDescriptionTask = tasks.named(
                TransformationTasks.TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                Transform.class);

        tasks.register(
                BUILD_POOL_TASK_NAME,
                BuildPool.class,
                task -> {
                    task.setGroup(Registrar.BUILD_GROUP_NAME);
                    task.getPoolOperations().set(pool.getPoolOperations());
                    task.getPoolDescriptionFile().convention(transformPoolDescriptionTask.get().getOutputFile());
                    task.getPoolRunFile().convention(pool.getPoolRunFile());
                    task.dependsOn(buildRootImage, buildDataSourceImage);
                });
    }

    public void registerBuildRootImageTask(TaskContainer tasks) {
        var downloadDistributionTask = tasks.named(
                DownloadTasksRegistrar.DOWNLOAD_DISTRIBUTION_TASK_NAME, DownloadDistribution.class);

        tasks.register(
                BUILD_ROOT_IMAGE_TASK_NAME,
                BuildRootImage.class,
                task -> {
                    task.setGroup(Registrar.BUILD_GROUP_NAME);
                    task.getSize().convention(Registrar.DEFAULT_IMAGE_SIZE);
                    task.getBaseImage().convention(downloadDistributionTask.get().getBaseImage());
                    task.getRootImage().convention(pool.getPoolDirectory().file(pool.getRootImageName()));
                    task.getRootImageMd5File().convention(pool.getRootImageMd5File());

                });
    }

    public void registerBuildDataSourceImageTask(TaskContainer tasks, String group) {

        var transformNetworkConfigTask =  tasks.named(
                TransformationTasks.TRANSFORM_NETWORK_CONFIG_TASK_NAME,
                Transform.class);

        var transformUserDataTask = tasks.named(
                TransformationTasks.TRANSFORM_USER_DATA_TASK_NAME,
                Transform.class);

        tasks.register(
                BUILD_DATA_SOURCE_IMAGE_TASK_NAME,
                BuildDataSourceImage.class,
                task -> {
                    task.setGroup(group);
                    task.getNetworkConfig().convention(transformNetworkConfigTask.get().getOutputFile());
                    task.getUserData().convention(transformUserDataTask.get().getOutputFile());
                    task.getDataSourceImage().convention(
                            pool.getPoolDirectory()
                                    .file(pool.getCidataImageName()));
                });
    }
}
