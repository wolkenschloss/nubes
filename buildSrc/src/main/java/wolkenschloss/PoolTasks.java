package wolkenschloss;

import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.download.DownloadDistribution;
import wolkenschloss.download.DownloadTasksRegistrar;
import wolkenschloss.pool.BuildDataSourceImage;
import wolkenschloss.pool.BuildPool;
import wolkenschloss.pool.BuildRootImage;
import wolkenschloss.pool.PoolExtension;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasksRegistrar;

public class PoolTasks {
    private final PoolExtension pool;

    public PoolTasks(PoolExtension pool) {
        this.pool = pool;
    }

    public void registerBuildPoolTask(TaskContainer tasks) {

        var buildDataSourceImage = tasks.findByName(Registrar.BUILD_DATA_SOURCE_IMAGE_TASK_NAME);
        var buildRootImage = tasks.findByName(Registrar.BUILD_ROOT_IMAGE_TASK_NAME);

        var transformPoolDescriptionTask = tasks.named(
                TransformationTasksRegistrar.TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                Transform.class);

        tasks.register(
                Registrar.BUILD_POOL_TASK_NAME,
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
                Registrar.BUILD_ROOT_IMAGE_TASK_NAME,
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
                TransformationTasksRegistrar.TRANSFORM_NETWORK_CONFIG_TASK_NAME,
                Transform.class);

        var transformUserDataTask = tasks.named(
                TransformationTasksRegistrar.TRANSFORM_USER_DATA_TASK_NAME,
                Transform.class);

        tasks.register(
                Registrar.BUILD_DATA_SOURCE_IMAGE_TASK_NAME,
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
