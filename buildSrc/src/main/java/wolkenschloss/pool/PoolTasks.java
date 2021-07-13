package wolkenschloss.pool;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.download.DownloadDistribution;
import wolkenschloss.download.DownloadTasksRegistrar;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasks;

public class PoolTasks {
    public static final String BUILD_POOL_TASK_NAME = "buildPool";
    public static final String BUILD_ROOT_IMAGE_TASK_NAME = "buildRootImage";
    public static final String BUILD_DATA_SOURCE_IMAGE_TASK_NAME = "buildDataSourceImage";

    private  static final String DEFAULT_IMAGE_SIZE = "20G";

    private static final String GROUP_NAME = "pool";

    private final PoolExtension pool;

    public PoolTasks(PoolExtension pool) {
        this.pool = pool;
    }

    public void register(TaskContainer tasks) {
        registerBuildDataSourceImageTask(tasks);
        registerBuildRootImageTask(tasks);
        registerBuildPoolTask(tasks);
    }

    public void registerBuildPoolTask(TaskContainer tasks) {

        var buildDataSourceImage = tasks.findByName(BUILD_DATA_SOURCE_IMAGE_TASK_NAME);
        var buildRootImage = tasks.findByName(BUILD_ROOT_IMAGE_TASK_NAME);

        var transformPoolDescription = tasks.named(
                TransformationTasks.TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                Transform.class);

        var poolDescriptionFile = transformPoolDescription.map(t -> t.getOutputFile().get());

        tasks.register(
                BUILD_POOL_TASK_NAME,
                BuildPool.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Defines a virtlib storage pool based on the transformed description file containing a root image and a cloud-init data source volume.");
                    task.getPoolOperations().set(pool.getPoolOperations());
                    task.getPoolDescriptionFile().convention(poolDescriptionFile);
                    task.getPoolRunFile().convention(pool.getPoolRunFile());
                    task.dependsOn(buildRootImage, buildDataSourceImage);
                });
    }

    public void registerBuildRootImageTask(TaskContainer tasks) {
        var downloadDistributionTask = tasks.named(
                DownloadTasksRegistrar.DOWNLOAD_DISTRIBUTION_TASK_NAME, DownloadDistribution.class);

        var baseImage = downloadDistributionTask.map(t -> t.getBaseImage().get());
        var rootImage = pool.getPoolDirectory().file(pool.getRootImageName());

        tasks.register(
                BUILD_ROOT_IMAGE_TASK_NAME,
                BuildRootImage.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Creates the root image for the later domain from a downloaded base image.");
                    task.getSize().convention(DEFAULT_IMAGE_SIZE);
                    task.getBaseImage().convention(baseImage);
                    task.getRootImage().convention(rootImage);
                    task.getRootImageMd5File().convention(pool.getRootImageMd5File());

                });
    }

    public void registerBuildDataSourceImageTask(TaskContainer tasks) {

        var transformNetworkConfigTask = tasks.named(
                TransformationTasks.TRANSFORM_NETWORK_CONFIG_TASK_NAME,
                Transform.class);
        var networkConfig = transformNetworkConfigTask.map(t -> t.getOutputFile().get());

        var transformUserDataTask = tasks.named(
                TransformationTasks.TRANSFORM_USER_DATA_TASK_NAME,
                Transform.class);
        var userData = transformUserDataTask.map(t -> t.getOutputFile().get());

        var dataSourceImage = pool.getPoolDirectory().file(pool.getCidataImageName());

        tasks.register(
                BUILD_DATA_SOURCE_IMAGE_TASK_NAME,
                BuildDataSourceImage.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Generates a cloud-init data source volume containing the transformed network-config and user-data files.");
                    task.getNetworkConfig().convention(networkConfig);
                    task.getUserData().convention(userData);
                    task.getDataSourceImage().convention(dataSourceImage);
                });
    }
}
