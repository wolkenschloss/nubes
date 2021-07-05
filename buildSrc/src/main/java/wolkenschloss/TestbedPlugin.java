package wolkenschloss;

import org.gradle.api.*;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import wolkenschloss.task.*;
import wolkenschloss.task.status.StatusTask;
import wolkenschloss.task.start.Start;

public class TestbedPlugin implements Plugin<Project> {

     public static final String TRANSFORM_NETWORK_CONFIG_TASK_NAME = "CloudInit";
    public static final String TRANSFORM_USER_DATA_TASK_NAME = "UserData";
    public static final String CREATE_DATA_SOURCE_IMAGE_TASK_NAME = "cidata";
    public static final String DOWNLOAD_DISTRIBUTION_TASK_NAME = "download";
    public static final String CREATE_ROOT_IMAGE_TASK_NAME = "root";
    public static final String TRANSFORM_POOL_DESCRIPTION_TASK_NAME = "Pool";
    public static final String CREATE_POOL_TASK_NAME = "createPool";
    public static final String START_DOMAIN_TASK_NAME = "startDomain";
    public static final String READ_KUBE_CONFIG_TASK_NAME = "readKubeConfig";
    public static final String STATUS_TASK_NAME = "status";
    public static final String START_TASK_NAME = "start";
    public static final String DESTROY_TASK_NAME = "destroy";
    public static final String TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME = "Domain";

    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create("testbed", TestbedExtension.class);

        var distribution = new Distribution(project.getObjects(), extension.getBaseImage().getName());

        extension.configure(project.getLayout());

        var transformNetworkConfig = createTransformationTask(project, TRANSFORM_NETWORK_CONFIG_TASK_NAME,
                extension.getSourceDirectory().file("network-config.mustache"),
                extension.getGeneratedCloudInitDirectory().file("network-config"));

        var transformUserData = createTransformationTask(project, TRANSFORM_USER_DATA_TASK_NAME,
                extension.getSourceDirectory().file("user-data.mustache"),
                extension.getGeneratedCloudInitDirectory().file("user-data"));

        var createDataSourceImage = project.getTasks().register(
                CREATE_DATA_SOURCE_IMAGE_TASK_NAME,
                CreateDataSourceImage.class,
                task -> task.initialize(extension, transformNetworkConfig, transformUserData));

        var downloadDistribution = project.getTasks().register(
                DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class,
                task -> task.initialize(extension, distribution));

        var createRootImage = project.getTasks().register(
                CREATE_ROOT_IMAGE_TASK_NAME,
                CreateRootImage.class,
                task -> task.initialize(extension, downloadDistribution));

        var transformPoolDescription = createTransformationTask(project, TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                extension.getSourceDirectory().file("pool.xml.mustache"),
                extension.getGeneratedVirshConfigDirectory().file("pool.xml"));

        var transformDomainDescription = createTransformationTask(project, TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME,
                extension.getSourceDirectory().file("domain.xml.mustache"),
                extension.getGeneratedVirshConfigDirectory().file("domain.xml"));

        var createPool = project.getTasks().register(
                CREATE_POOL_TASK_NAME,
                CreatePool.class,
                task -> task.initialize(extension, createDataSourceImage, createRootImage, transformPoolDescription));

        var startDomain = project.getTasks().register(
                START_DOMAIN_TASK_NAME,
                Start.class,
                task -> task.initialize(extension, transformDomainDescription, createPool));

        var readKubeConfig = project.getTasks().register(
                READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> task.initialize(extension, startDomain));

        var status = project.getTasks().register(
                STATUS_TASK_NAME,
                StatusTask.class,
                task -> task.initialize(extension, startDomain, readKubeConfig));

        project.getTasks().withType(Transform.class).configureEach(
                configureTransformTask(extension, project.getObjects()));

        project.getTasks().register(START_TASK_NAME, DefaultTask.class,
                task -> task.dependsOn(readKubeConfig));

        project.getTasks().register(DESTROY_TASK_NAME, Destroy.class, task -> {
            task.initialize(project, extension, createPool);
        });
    }

    private Action<Transform> configureTransformTask(TestbedExtension extension, ObjectFactory objects) {
        return (Transform task) -> {
            task.getScope().convention(extension.asPropertyMap(objects));
        };
    }

    private TaskProvider<Transform> createTransformationTask(
            Project project,
            String name,
            Provider<RegularFile> template,
            Provider<RegularFile> output) {
        return project.getTasks().register("transform" + name, Transform.class, task -> {
            task.getTemplate().convention(template);
            task.getOutputFile().convention(output);
        });
    }
}
