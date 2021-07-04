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

    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create("testbed", TestbedExtension.class);

        var distribution = new Distribution(project.getObjects(), extension.getBaseImage().getName());

        extension.configure(project.getLayout());

        var networkConfig = createTransformationTask(project, TRANSFORM_NETWORK_CONFIG_TASK_NAME,
                extension.getSourceDirectory().file("network-config.mustache"),
                extension.getGeneratedCloudInitDirectory().file("network-config"));

        var userData = createTransformationTask(project, TRANSFORM_USER_DATA_TASK_NAME,
                extension.getSourceDirectory().file("user-data.mustache"),
                extension.getGeneratedCloudInitDirectory().file("user-data"));

        var cidata = project.getTasks().register(CREATE_DATA_SOURCE_IMAGE_TASK_NAME, CreateDataSource.class, task -> {
            // InputFiles
            task.getNetworkConfig().convention(networkConfig.get().getOutputFile());
            task.getUserData().convention(userData.get().getOutputFile());

            // OutputFile
            task.getCidata().convention(extension.getPoolDirectory().file(extension.getPool().getCidataImageName()));
        });

        var download = project.getTasks().register(DOWNLOAD_DISTRIBUTION_TASK_NAME, Download.class, task -> {
            task.getBaseImageLocation().convention(extension.getBaseImage().getUrl());
            task.getDistributionName().convention(extension.getBaseImage().getName());
            var parts = extension.getBaseImage().getUrl().get().split("/");
            var basename = parts[parts.length - 1];
            task.getBaseImage().convention(distribution.file(basename));
        });

        var root = project.getTasks().register(CREATE_ROOT_IMAGE_TASK_NAME, CreateRootImage.class, task -> {
            task.getSize().convention("20G");
            task.getBaseImage().convention(download.get().getBaseImage());
            task.getRootImage().convention(extension.getPoolDirectory().file(extension.getPool().getRootImageName()));
            task.getRootImageMd5File().convention(extension.getRunDirectory().file("root.md5"));
        });

        var poolConfig = createTransformationTask(project, TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                extension.getSourceDirectory().file("pool.xml.mustache"),
                extension.getGeneratedVirshConfigDirectory().file("pool.xml"));

        var domainConfig = createTransformationTask(project, "Domain",
                extension.getSourceDirectory().file("domain.xml.mustache"),
                extension.getGeneratedVirshConfigDirectory().file("domain.xml"));

        var createPool = project.getTasks().register(CREATE_POOL_TASK_NAME, CreatePool.class, task -> {
            task.getPoolName().convention(extension.getPool().getName());
            task.getXmlDescription().convention(poolConfig.get().getOutputFile());
            task.getPoolRunFile().convention(extension.getRunDirectory().file("pool.run"));
            task.getDomainName().convention(extension.getDomain().getName());
            task.dependsOn(root, cidata);
        });

        var startDomain = project.getTasks().register(START_DOMAIN_TASK_NAME, Start.class, task -> {
            task.dependsOn(createPool);
            task.getDomain().convention(extension.getDomain().getName());
            task.getHostname().convention(extension.getDomain().getName());
            task.getPort().convention(extension.getHost().getCallbackPort());
            task.getXmlDescription().convention(domainConfig.get().getOutputFile());
            task.getPoolRunFile().convention(createPool.get().getPoolRunFile());
            task.getKnownHostsFile().convention(extension.getRunDirectory().file("known_hosts"));
        });

        var readKubeConfig = project.getTasks().register(READ_KUBE_CONFIG_TASK_NAME, CopyKubeConfig.class, task -> {
            // benötigt funktionierenden ssh Zugang. Deswegen muss updateKnownHosts
            // vorher ausgeführt sein.
//            task.dependsOn(updateKnownHosts);
            task.getDomainName().convention(extension.getDomain().getName());
            task.getKubeConfigFile().convention(extension.getRunDirectory().file("kubeconfig"));
            task.getKnownHostsFile().convention(startDomain.get().getKnownHostsFile());
        });

        var status = project.getTasks().register(STATUS_TASK_NAME, StatusTask.class, task -> {
            task.getDomainName().convention(extension.getDomain().getName());
            task.getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
            task.getKnownHostsFile().convention(startDomain.get().getKnownHostsFile());
            task.getPoolName().convention(extension.getPool().getName());
            task.getDistributionName().convention(extension.getBaseImage().getName());
        });

        project.getTasks().withType(Transform.class).configureEach(
                configureTransformTask(extension, project.getObjects()));

        project.getTasks().register(START_TASK_NAME, DefaultTask.class,
                task -> task.dependsOn(readKubeConfig));

        project.getTasks().register(DESTROY_TASK_NAME, Destroy.class, task -> {
            task.getDomain().convention(extension.getDomain().getName());
            task.getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
            task.getKnownHostsFile().convention(startDomain.get().getKnownHostsFile());
            task.getDomainXmlConfig().convention(domainConfig.get().getOutputFile());
            task.getPoolRunFile().convention(createPool.get().getPoolRunFile());
            task.getPoolXmlConfig().convention(poolConfig.get().getOutputFile());
            task.getRootImageFile().convention(root.get().getRootImage());
            task.getRootImageMd5File().convention(root.get().getRootImageMd5File());
            task.getCiDataImageFile().convention(cidata.get().getCidata());
            task.getNetworkConfig().convention(networkConfig.get().getOutputFile());
            task.getUserData().convention(userData.get().getOutputFile());
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
