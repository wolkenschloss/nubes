package wolkenschloss;

import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import wolkenschloss.domain.BuildDomain;
import wolkenschloss.domain.DomainOperations;
import wolkenschloss.pool.BuildDataSourceImage;
import wolkenschloss.pool.BuildPool;
import wolkenschloss.pool.BuildRootImage;
import wolkenschloss.pool.DownloadDistribution;
import wolkenschloss.pool.PoolOperations;
import wolkenschloss.transformation.TaskRegistrar;

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

    private final Project project;
    private final TestbedExtension extension;

    public Registrar(Project project, TestbedExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    private static String templateFilename(String filename) {
        return String.format("%s.%s", filename, TEMPLATE_FILENAME_EXTENSION);
    }

    TaskProvider<BuildPool> getBuildPoolTaskProvider(TaskProvider<BuildDataSourceImage> buildDataSourceImage, TaskProvider<BuildRootImage> buildRootImage) {
        var transformPoolDescription = TaskRegistrar.create()
                .name(TRANSFORM_POOL_DESCRIPTION_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms pool.xml template")
                .template(getExtension().getSourceDirectory().file(templateFilename(POOL_DESCRIPTION_FILE_NAME)))
                .output(getExtension().getGeneratedVirshConfigDirectory().file(POOL_DESCRIPTION_FILE_NAME))
                .register(getProject());

        return getProject().getTasks().register(
                BUILD_POOL_TASK_NAME,
                BuildPool.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getPoolOperations().set(getExtension().getPoolOperations());
                    task.getPoolDescriptionFile().convention(transformPoolDescription.get().getOutputFile());
                    task.getPoolRunFile().convention(getExtension().getRunDirectory().file("pool.run"));
                    task.dependsOn(buildRootImage, buildDataSourceImage);
                });
    }

    TaskProvider<BuildRootImage> getBuildRootImageTaskProvider() {
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

        return getProject().getTasks().register(
                BUILD_ROOT_IMAGE_TASK_NAME,
                BuildRootImage.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getSize().convention(DEFAULT_IMAGE_SIZE);
                    task.getBaseImage().convention(downloadDistribution.get().getBaseImage());

                    task.getRootImage().convention(
                            getExtension().getPoolDirectory()
                            .file(getExtension().getPool().getRootImageName()));

                    task.getRootImageMd5File().convention(getExtension().getRunDirectory().file(DEFAULT_RUN_FILE_NAME));
                });
    }

    TaskProvider<BuildDataSourceImage> getBuildDataSourceImageTaskProvider() {
        var transformNetworkConfig = TaskRegistrar.create()
                .name(TRANSFORM_NETWORK_CONFIG_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms network-config template")
                .template(getExtension().getSourceDirectory().file(templateFilename(NETWORK_CONFIG_FILE_NAME)))
                .output(getExtension().getGeneratedCloudInitDirectory().file(NETWORK_CONFIG_FILE_NAME))
                .register(getProject());

        var transformUserData = TaskRegistrar.create()
                .name(TRANSFORM_USER_DATA_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms user-data template")
                .template(getExtension().getSourceDirectory().file(templateFilename(USER_DATA_FILE_NAME)))
                .output(getExtension().getGeneratedCloudInitDirectory().file(USER_DATA_FILE_NAME))
                .register(getProject());

        return getProject().getTasks().register(
                BUILD_DATA_SOURCE_IMAGE_TASK_NAME,
                BuildDataSourceImage.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getNetworkConfig().convention(transformNetworkConfig.get().getOutputFile());
                    task.getUserData().convention(transformUserData.get().getOutputFile());

                    task.getDataSourceImage().convention(
                            getExtension().getPoolDirectory()
                                    .file(getExtension().getPool().getCidataImageName()));
                });
    }

    TaskProvider<BuildDomain> getBuildDomainTaskProvider(Provider<DomainOperations> domainOperations, TaskProvider<BuildPool> buildPool) {
        var transformDomainDescription = TaskRegistrar.create()
                .name(TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME)
                .group(BUILD_GROUP_NAME)
                .description("Transforms domain.xml")
                .template(getExtension().getSourceDirectory().file(templateFilename(DOMAIN_DESCRIPTION_FILE_NAME)))
                .output(getExtension().getGeneratedVirshConfigDirectory().file(DOMAIN_DESCRIPTION_FILE_NAME))
                .register(getProject());

        return getProject().getTasks().register(
                BUILD_DOMAIN_TASK_NAME,
                BuildDomain.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.dependsOn(buildPool);
                    task.setDescription("Starts the libvirt domain and waits for the callback.");
                    task.getDomain().convention(getExtension().getDomain().getName());
                    task.getPort().convention(getExtension().getHost().getCallbackPort());
                    task.getXmlDescription().convention(transformDomainDescription.get().getOutputFile());
                    task.getKnownHostsFile().convention(getExtension().getRunDirectory().file(TestbedPlugin.DEFAULT_KNOWN_HOSTS_FILE_NAME));
                    task.getDomainOperations().set(domainOperations);
                });
    }

    public Project getProject() {
        return project;
    }

    public TestbedExtension getExtension() {
        return extension;
    }
}
