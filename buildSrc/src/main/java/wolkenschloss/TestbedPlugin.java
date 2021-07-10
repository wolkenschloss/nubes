package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import wolkenschloss.task.*;
import wolkenschloss.task.start.Start;
import wolkenschloss.task.status.StatusTask;

public class TestbedPlugin implements Plugin<Project> {

    public static final String TRANSFORM_NETWORK_CONFIG_TASK_NAME = "transformNetworkConfig";
    public static final String TRANSFORM_USER_DATA_TASK_NAME = "transformUserData";
    public static final String TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME = "transformDomainDescription";
    public static final String TRANSFORM_POOL_DESCRIPTION_TASK_NAME = "transformPoolDescription";
    public static final String TEMPLATE_FILENAME_EXTENSION = "mustache";

    public static final String CREATE_DATA_SOURCE_IMAGE_TASK_NAME = "cidata";
    public static final String DOWNLOAD_DISTRIBUTION_TASK_NAME = "download";
    public static final String CREATE_ROOT_IMAGE_TASK_NAME = "root";
    public static final String CREATE_POOL_TASK_NAME = "createPool";
    public static final String START_DOMAIN_TASK_NAME = "startDomain";
    public static final String READ_KUBE_CONFIG_TASK_NAME = "readKubeConfig";
    public static final String STATUS_TASK_NAME = "status";
    public static final String START_TASK_NAME = "start";
    public static final String DESTROY_TASK_NAME = "destroy";
    public static final String NETWORK_CONFIG_FILE_NAME = "network-config";
    public static final String USER_DATA_FILE_NAME = "user-data";
    public static final String POOL_DESCRIPTION_FILE_NAME = "pool.xml";
    public static final String DOMAIN_DESCRIPTION_FILE_NAME = "domain.xml";
    public static final String TESTBED_EXTENSION_NAME = "testbed";

    private static String templateFilename(String filename) {
        return String.format("%s.%s", filename, TEMPLATE_FILENAME_EXTENSION);
    }

    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create(TESTBED_EXTENSION_NAME, TestbedExtension.class);

        extension.configure(project.getLayout());
        var registrar = new TransformationTaskRegistrar(project);

        var transformNetworkConfig = registrar.register(
                TRANSFORM_NETWORK_CONFIG_TASK_NAME,
                extension.getSourceDirectory().file(templateFilename(NETWORK_CONFIG_FILE_NAME)),
                extension.getGeneratedCloudInitDirectory().file(NETWORK_CONFIG_FILE_NAME));

        var transformUserData = registrar.register(
                TRANSFORM_USER_DATA_TASK_NAME,
                extension.getSourceDirectory().file(templateFilename(USER_DATA_FILE_NAME)),
                extension.getGeneratedCloudInitDirectory().file(USER_DATA_FILE_NAME));

        var createDataSourceImage = project.getTasks().register(
                CREATE_DATA_SOURCE_IMAGE_TASK_NAME,
                CreateDataSourceImage.class,
                task -> task.initialize(extension, transformNetworkConfig, transformUserData));

        var downloadDistribution = project.getTasks().register(
                DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class,
                task -> task.initialize(extension.getBaseImage()));

        var createRootImage = project.getTasks().register(
                CREATE_ROOT_IMAGE_TASK_NAME,
                CreateRootImage.class,
                task -> task.initialize(extension, downloadDistribution));

        var transformPoolDescription = registrar.register(
                TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                extension.getSourceDirectory().file(templateFilename(POOL_DESCRIPTION_FILE_NAME)),
                extension.getGeneratedVirshConfigDirectory().file(POOL_DESCRIPTION_FILE_NAME));

        var transformDomainDescription = registrar.register(
                TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME,
                extension.getSourceDirectory().file(templateFilename(DOMAIN_DESCRIPTION_FILE_NAME)),
                extension.getGeneratedVirshConfigDirectory().file(DOMAIN_DESCRIPTION_FILE_NAME));

        var createPool = project.getTasks().register(
                CREATE_POOL_TASK_NAME,
                CreatePool.class,
                task -> task.initialize(extension, createDataSourceImage, createRootImage, transformPoolDescription));

        var startDomain = project.getTasks().register(
                START_DOMAIN_TASK_NAME,
                Start.class,
                task -> task.initialize(extension.getStartParameter(), transformDomainDescription, createPool));

        var readKubeConfig = project.getTasks().register(
                READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> task.initialize(extension.getCopyKubeConfigParameter(), extension, startDomain));

        project.getTasks().register(
                STATUS_TASK_NAME,
                StatusTask.class,
                task -> task.initialize( extension.getStatusParameter(), startDomain, readKubeConfig));

        project.getTasks().withType(Transform.class).configureEach(
                task -> task.getScope().convention(extension.asPropertyMap(project.getObjects())));

        project.getTasks().register(
                START_TASK_NAME,
                DefaultTask.class,
                task -> task.dependsOn(readKubeConfig));

        project.getTasks().register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> task.initialize(project, extension, createPool));
    }
}
