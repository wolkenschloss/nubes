package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import wolkenschloss.domain.BuildDomain;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.pool.*;
import wolkenschloss.status.Status;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasksRegistrar;

public class Registrar {
    public static final String BUILD_GROUP_NAME = "build";

    public static final String BUILD_DATA_SOURCE_IMAGE_TASK_NAME = "buildDataSourceImage";
    public static final String DOWNLOAD_DISTRIBUTION_TASK_NAME = "download";
    public static final String BUILD_ROOT_IMAGE_TASK_NAME = "buildRootImage";
    public static final String BUILD_POOL_TASK_NAME = "buildPool";
    public static final String BUILD_DOMAIN_TASK_NAME = "buildDomain";


    public static final String DEFAULT_IMAGE_SIZE = "20G";

    public static final String READ_KUBE_CONFIG_TASK_NAME = "readKubeConfig";
    public static final String DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig";

    public static final String STATUS_TASK_NAME = "status";
    public static final String START_TASK_NAME = "start";
    public static final String DESTROY_TASK_NAME = "destroy";

    private final Project project;
    private final TestbedExtension extension;

    public Registrar(Project project, TestbedExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    public void register() {
        new TransformationTasksRegistrar(project, BUILD_GROUP_NAME)
                .registerTransformationTasks(extension.getTransformation());


        TaskContainer tasks = getProject().getTasks();
        PoolExtension pool = getExtension().getPool();
        registerBuildDataSourceImageTask(tasks, BUILD_GROUP_NAME, pool);
        registerDownloadDistributionTask(tasks, getExtension().getBaseImage());
        registerBuildRootImageTask(tasks, pool);
        registerBuildPoolTask(tasks, pool);

        registerBuildDomainTask(getProject().getTasks(), getExtension().getDomain(), getExtension().getHost());

        var readKubeConfig = getCopyKubeConfigTaskProvider();

        getProject().getTasks().withType(Transform.class).configureEach(
                task -> task.getScope().convention(getExtension().asPropertyMap(getProject().getObjects())));

        getProject().getTasks().register(
                START_TASK_NAME,
                DefaultTask.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.dependsOn(readKubeConfig);
                });

        registerDestroyTask();
    }

    <S extends Task> S findTask(Class<S> type, String name) {
        return getProject().getTasks().withType(type).getByName(name);
    }

    Transform findTransformTask(String name) {
        return findTask(Transform.class, name);
    }

    private void registerDestroyTask() {
        getProject().getTasks().register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getPoolOperations().set(getExtension().getPool().getPoolOperations());
                    task.getDomain().convention(getExtension().getDomain().getName());
                    task.getPoolRunFile().convention(findTask(BuildPool.class, BUILD_POOL_TASK_NAME).getPoolRunFile());
                    task.getBuildDir().convention(getProject().getLayout().getBuildDirectory());
                    task.getDomainOperations().set(getExtension().getDomain().getDomainOperations());
                });
    }

    private TaskProvider<CopyKubeConfig> getCopyKubeConfigTaskProvider() {

        var buildDomain = getProject().getTasks()
                .withType(BuildDomain.class)
                .getByName(BUILD_DOMAIN_TASK_NAME);

        var readKubeConfig = getProject().getTasks().register(
                READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> {
                    task.getDomainName().convention(getExtension().getDomain().getName());
                    task.getKubeConfigFile().convention(getExtension().getRunDirectory().file(DEFAULT_KUBE_CONFIG_FILE_NAME));
                    task.getKnownHostsFile().convention(buildDomain.getKnownHostsFile());
                    task.getSecureShellService().set(getExtension().getSecureShellService());
                });

        getProject().getTasks().register(
                STATUS_TASK_NAME,
                Status.class,
                task -> {
                    task.getPoolOperations().set(getExtension().getPool().getPoolOperations());
                    task.getDomainOperations().set(getExtension().getDomain().getDomainOperations());
                    task.getRegistryService().set(getExtension().getRegistryService());
                    task.getSecureShellService().set(getExtension().getSecureShellService());
                    task.getDomainName().convention(getExtension().getDomain().getName());
                    task.getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
                    task.getKnownHostsFile().convention(buildDomain.getKnownHostsFile());
                    task.getDistributionName().convention(getExtension().getBaseImage().getName());
                    task.getDownloadDir().convention(getExtension().getBaseImage().getDownloadDir());
                    task.getDistributionDir().convention(getExtension().getBaseImage().getDistributionDir());
                    task.getBaseImageFile().convention(getExtension().getBaseImage().getBaseImageFile());
                });

        return readKubeConfig;
    }

    public static void registerBuildPoolTask(TaskContainer tasks, PoolExtension pool) {
        var buildDataSourceImage = tasks.findByName(BUILD_DATA_SOURCE_IMAGE_TASK_NAME);
        var buildRootImage = tasks.findByName(BUILD_ROOT_IMAGE_TASK_NAME);

        var transformPoolDescriptionTask = tasks.named(
                TransformationTasksRegistrar.TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                Transform.class);

        tasks.register(
                BUILD_POOL_TASK_NAME,
                BuildPool.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getPoolOperations().set(pool.getPoolOperations());
                    task.getPoolDescriptionFile().convention(transformPoolDescriptionTask.get().getOutputFile());
                    task.getPoolRunFile().convention(pool.getRootImageMd5File());
                    task.dependsOn(buildRootImage, buildDataSourceImage);
                });
    }

    public static void registerBuildRootImageTask(TaskContainer tasks, PoolExtension pool) {
        var downloadDistributionTask = tasks.named(DOWNLOAD_DISTRIBUTION_TASK_NAME, DownloadDistribution.class);

        tasks.register(
                BUILD_ROOT_IMAGE_TASK_NAME,
                BuildRootImage.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getSize().convention(DEFAULT_IMAGE_SIZE);
                    task.getBaseImage().convention(downloadDistributionTask.get().getBaseImage());
                    task.getRootImage().convention(pool.getPoolDirectory().file(pool.getRootImageName()));
                    task.getRootImageMd5File().convention(pool.getRootImageMd5File());

                });
    }

    public static void registerDownloadDistributionTask(TaskContainer tasks, BaseImageExtension baseImage1) {
        tasks.register(
                DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class,
                task -> {
                    var baseImage = baseImage1;
                    task.getBaseImageLocation().convention(baseImage.getUrl());
                    task.getDistributionName().convention(baseImage.getName());
                    task.getBaseImage().convention(baseImage.getBaseImageFile());
                    task.getDistributionDir().convention(baseImage.getDistributionDir());
                });
    }

    public static void registerBuildDataSourceImageTask(TaskContainer tasks, String group, PoolExtension poolExtension) {
        var transformNetworkConfigTask =  tasks.named(
                TransformationTasksRegistrar.TRANSFORM_NETWORK_CONFIG_TASK_NAME,
                Transform.class);

        var transformUserDataTask = tasks.named(
                TransformationTasksRegistrar.TRANSFORM_USER_DATA_TASK_NAME,
                Transform.class);

        tasks.register(
                BUILD_DATA_SOURCE_IMAGE_TASK_NAME,
                BuildDataSourceImage.class,
                task -> {
                    task.setGroup(group);
                    task.getNetworkConfig().convention(transformNetworkConfigTask.get().getOutputFile());
                    task.getUserData().convention(transformUserDataTask.get().getOutputFile());
                    task.getDataSourceImage().convention(
                            poolExtension.getPoolDirectory()
                                    .file(poolExtension.getCidataImageName()));
                });
    }

    private void registerBuildDomainTask(TaskContainer tasks, DomainExtension domain, HostExtension host) {
        var buildPool = tasks.findByName(BUILD_POOL_TASK_NAME);

        var domainDescription = tasks
                .named(
                    TransformationTasksRegistrar.TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME,
                    Transform.class)
                .map(Transform::getOutputFile)
                .get();

        tasks.register(
                BUILD_DOMAIN_TASK_NAME,
                BuildDomain.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.dependsOn(buildPool);
                    task.setDescription("Starts the libvirt domain and waits for the callback.");
                    task.getDomain().convention(domain.getName());
                    task.getPort().convention(host.getCallbackPort());
                    task.getXmlDescription().convention(domainDescription);

                    task.getKnownHostsFile().convention(extension.getRunDirectory()
                            .file(TestbedExtension.DEFAULT_KNOWN_HOSTS_FILE_NAME));

                    task.getDomainOperations().set(domain.getDomainOperations());
                });
    }

    private Project getProject() {
        return project;
    }

    private TestbedExtension getExtension() {
        return extension;
    }
}
