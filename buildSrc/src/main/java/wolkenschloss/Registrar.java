package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskProvider;
import wolkenschloss.domain.BuildDomain;
import wolkenschloss.pool.BuildDataSourceImage;
import wolkenschloss.pool.BuildPool;
import wolkenschloss.pool.BuildRootImage;
import wolkenschloss.pool.DownloadDistribution;
import wolkenschloss.status.Status;
import wolkenschloss.transformation.TaskRegistrar;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationExtension;

public class Registrar {
    public static final String BUILD_GROUP_NAME = "build";

    public static final String TRANSFORM_NETWORK_CONFIG_TASK_NAME = "transformNetworkConfig";
    public static final String TRANSFORM_USER_DATA_TASK_NAME = "transformUserData";
    public static final String TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME = "transformDomainDescription";
    public static final String TRANSFORM_POOL_DESCRIPTION_TASK_NAME = "transformPoolDescription";
    public static final String TEMPLATE_FILENAME_EXTENSION = "mustache";

    public static final String BUILD_DATA_SOURCE_IMAGE_TASK_NAME = "buildDataSourceImage";
    public static final String DOWNLOAD_DISTRIBUTION_TASK_NAME = "download";
    public static final String BUILD_ROOT_IMAGE_TASK_NAME = "buildRootImage";
    public static final String BUILD_POOL_TASK_NAME = "buildPool";
    public static final String BUILD_DOMAIN_TASK_NAME = "buildDomain";

    public static final String NETWORK_CONFIG_FILE_NAME = "network-config";
    public static final String USER_DATA_FILE_NAME = "user-data";
    public static final String POOL_DESCRIPTION_FILE_NAME = "pool.xml";
    public static final String DOMAIN_DESCRIPTION_FILE_NAME = "domain.xml";

    public static final String DEFAULT_IMAGE_SIZE = "20G";
    public static final String DEFAULT_RUN_FILE_NAME = "root.md5";
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

    private static String templateFilename(String filename) {
        return String.format("%s.%s", filename, TEMPLATE_FILENAME_EXTENSION);
    }

    public void register() {

        var transformExtension = extension.getTransformation();
        registerTransformUserDataTask(project, transformExtension);
        registerTransformNetworkConfigTask(project, transformExtension);
        registerTransformPoolDescriptionTask(project, transformExtension);
        registerTransformDomainDescriptionTask(project, transformExtension);

        getBuildDataSourceImageTaskProvider();
        getBuildRootImageTaskProvider();
        getBuildPoolTaskProvider();
        getBuildDomainTaskProvider();
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

    private void getBuildPoolTaskProvider() {
        var buildDataSourceImage = getProject().getTasks().findByName(BUILD_DATA_SOURCE_IMAGE_TASK_NAME);
        var buildRootImage = getProject().getTasks().findByName(BUILD_ROOT_IMAGE_TASK_NAME);

        getProject().getTasks().register(
                BUILD_POOL_TASK_NAME,
                BuildPool.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getPoolOperations().set(getExtension().getPool().getPoolOperations());
                    task.getPoolDescriptionFile().convention(findTransformTask(TRANSFORM_POOL_DESCRIPTION_TASK_NAME).getOutputFile());
                    task.getPoolRunFile().convention(getExtension().getRunDirectory().file("pool.run"));
                    task.dependsOn(buildRootImage, buildDataSourceImage);
                });
    }

    private void getBuildRootImageTaskProvider() {
        var downloadDistribution = getProject().getTasks().register(
                DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class,
                task -> {
                    var baseImage = getExtension().getBaseImage();
                    task.getBaseImageLocation().convention(baseImage.getUrl());
                    task.getDistributionName().convention(baseImage.getName());
                    task.getBaseImage().convention(baseImage.getBaseImageFile());
                    task.getDistributionDir().convention(baseImage.getDistributionDir());
                });

        getProject().getTasks().register(
                BUILD_ROOT_IMAGE_TASK_NAME,
                BuildRootImage.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getSize().convention(DEFAULT_IMAGE_SIZE);
                    task.getBaseImage().convention(downloadDistribution.get().getBaseImage());

                    task.getRootImage().convention(
                            getExtension().getPool().getPoolDirectory()
                                    .file(getExtension().getPool().getRootImageName()));

                    task.getRootImageMd5File().convention(getExtension().getRunDirectory().file(DEFAULT_RUN_FILE_NAME));
                });
    }

    private void getBuildDataSourceImageTaskProvider() {
        getProject().getTasks().register(
                BUILD_DATA_SOURCE_IMAGE_TASK_NAME,
                BuildDataSourceImage.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getNetworkConfig().convention(findTransformTask(TRANSFORM_NETWORK_CONFIG_TASK_NAME).getOutputFile());
                    task.getUserData().convention(findTransformTask(TRANSFORM_USER_DATA_TASK_NAME).getOutputFile());
                    task.getDataSourceImage().convention(
                            getExtension().getPool().getPoolDirectory()
                                    .file(getExtension().getPool().getCidataImageName()));
                });
    }

    private void getBuildDomainTaskProvider() {
        var buildPool = getProject().getTasks().findByName(BUILD_POOL_TASK_NAME);

        var project = getProject();

        getProject().getTasks().register(
                BUILD_DOMAIN_TASK_NAME,
                BuildDomain.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.dependsOn(buildPool);
                    task.setDescription("Starts the libvirt domain and waits for the callback.");
                    task.getDomain().convention(getExtension().getDomain().getName());
                    task.getPort().convention(getExtension().getHost().getCallbackPort());
                    task.getXmlDescription().convention(findTransformTask(TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME).getOutputFile());

                    task.getKnownHostsFile().convention(extension.getRunDirectory()
                            .file(TestbedExtension.DEFAULT_KNOWN_HOSTS_FILE_NAME));

                    task.getDomainOperations().set(getExtension().getDomain().getDomainOperations());
                });
    }

    private void registerTransformPoolDescriptionTask(Project project, TransformationExtension extension) {
        TaskRegistrar.create(extension)
                .name(TRANSFORM_POOL_DESCRIPTION_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms pool.xml template")
                .template(src -> src.file(templateFilename(POOL_DESCRIPTION_FILE_NAME)))
                .outputDescription(dst -> dst.file(POOL_DESCRIPTION_FILE_NAME))
                .register(project);
    }

    private void registerTransformDomainDescriptionTask(Project project, TransformationExtension extension) {
        TaskRegistrar.create(extension)
                .name(TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms domain.xml")
                .template(src -> src.file(templateFilename(DOMAIN_DESCRIPTION_FILE_NAME)))
                .outputDescription(dst -> dst.file(DOMAIN_DESCRIPTION_FILE_NAME))
                .register(project);
    }

    private void registerTransformUserDataTask(Project project, TransformationExtension extension) {
        TaskRegistrar.create(extension)
                .name(TRANSFORM_USER_DATA_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms user-data template")
                .template(src -> src.file(templateFilename(USER_DATA_FILE_NAME)))
                .outputCloudConfig(dst -> dst.file(USER_DATA_FILE_NAME))
                .register(project);
    }

    private void registerTransformNetworkConfigTask(Project project, TransformationExtension extension) {
        TaskRegistrar.create(extension)
                .name(TRANSFORM_NETWORK_CONFIG_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms network-config template")
                .template(src -> src.file(templateFilename(NETWORK_CONFIG_FILE_NAME)))
                .outputCloudConfig(dst -> dst.file(NETWORK_CONFIG_FILE_NAME))
                .register(project);
    }

    private Project getProject() {
        return project;
    }

    private TestbedExtension getExtension() {
        return extension;
    }
}
