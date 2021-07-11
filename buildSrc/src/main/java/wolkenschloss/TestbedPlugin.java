package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import wolkenschloss.domain.DomainOperations;
import wolkenschloss.domain.Start;
import wolkenschloss.model.SecureShellService;
import wolkenschloss.pool.*;
import wolkenschloss.status.RegistryService;
import wolkenschloss.status.StatusTask;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TaskRegistrar;

@SuppressWarnings("UnstableApiUsage")
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

    public static final String DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig";
    public static final String DEFAULT_IMAGE_SIZE = "20G";
    public static final String DEFAULT_RUN_FILE_NAME = "root.md5";
    public static final String DEFAULT_KNOWN_HOSTS_FILE_NAME = "known_hosts";

    @Override
    public void apply(Project project) {
        var providers = project.getProviders();
        TestbedExtension extension = project.getExtensions()
                .create(TESTBED_EXTENSION_NAME, TestbedExtension.class)
                .configure(project.getLayout());

        var sharedServices = project.getGradle().getSharedServices();

        var domainOperations = sharedServices.registerIfAbsent(
                "domainops",
                DomainOperations.class,
                spec -> spec.getParameters().getDomainName().set(extension.getDomain().getName()));

        var secureShellService = sharedServices.registerIfAbsent(
                "sshservice",
                SecureShellService.class,
                spec -> {
                    spec.getParameters().getDomainOperations().set(domainOperations);
                    spec.getParameters().getKnownHostsFile().set(extension.getRunDirectory().file(DEFAULT_KNOWN_HOSTS_FILE_NAME));
                });

        var poolOperations = sharedServices.registerIfAbsent(
                "poolops",
                PoolOperations.class,
                spec -> spec.getParameters().getPoolName().set(extension.getPool().getName()));

        var registryService = sharedServices.registerIfAbsent(
                "registryService",
                RegistryService.class,
                spec -> spec.getParameters().getDomainOperations().set(domainOperations));

        var registrar = new TaskRegistrar(project);

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
                task -> {
                    task.getNetworkConfig().convention(transformNetworkConfig.get().getOutputFile());
                    task.getUserData().convention(transformUserData.get().getOutputFile());
                    task.getCidata().convention(extension.getPoolDirectory().file(extension.getPool().getCidataImageName()));
                });

        var downloadDistribution = project.getTasks().register(
                DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class,
                task -> {
                    var baseImage = extension.getBaseImage();
                    task.getBaseImageLocation().convention(baseImage.getUrl());
                    task.getDistributionName().convention(baseImage.getName());
                    task.getBaseImage().convention(baseImage.getBaseImageFile());
                    task.getDistributionDir().convention(baseImage.getDistributionDir());
                });

        var createRootImage = project.getTasks().register(
                CREATE_ROOT_IMAGE_TASK_NAME,
                CreateRootImage.class,
                task -> {
                    task.getSize().convention(DEFAULT_IMAGE_SIZE);
                    task.getBaseImage().convention(downloadDistribution.get().getBaseImage());
                    task.getRootImage().convention(extension.getPoolDirectory().file(extension.getPool().getRootImageName()));
                    task.getRootImageMd5File().convention(extension.getRunDirectory().file(DEFAULT_RUN_FILE_NAME));
                });

        var transformPoolDescription = registrar.register(
                TRANSFORM_POOL_DESCRIPTION_TASK_NAME,
                extension.getSourceDirectory().file(templateFilename(POOL_DESCRIPTION_FILE_NAME)),
                extension.getGeneratedVirshConfigDirectory().file(POOL_DESCRIPTION_FILE_NAME));

        var createPool = project.getTasks().register(
                CREATE_POOL_TASK_NAME,
                CreatePool.class,
                task -> {
                    task.getPoolOperations().set(poolOperations);
                    task.getXmlDescription().convention(transformPoolDescription.get().getOutputFile());
                    task.getPoolRunFile().convention(extension.getRunDirectory().file("pool.run"));
                    task.dependsOn(createRootImage, createDataSourceImage);
                });

        var transformDomainDescription = registrar.register(
                TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME,
                extension.getSourceDirectory().file(templateFilename(DOMAIN_DESCRIPTION_FILE_NAME)),
                extension.getGeneratedVirshConfigDirectory().file(DOMAIN_DESCRIPTION_FILE_NAME));

        var startDomain = project.getTasks().register(
                START_DOMAIN_TASK_NAME,
                Start.class,
                task -> {
                    task.dependsOn(createPool);
                    task.setDescription("Starts the libvirt domain and waits for the callback.");
                    task.getDomain().convention(extension.getDomain().getName());
                    task.getPort().convention(extension.getHost().getCallbackPort());
                    task.getXmlDescription().convention(transformDomainDescription.get().getOutputFile());
                    task.getPoolRunFile().convention(createPool.get().getPoolRunFile());
                    task.getKnownHostsFile().convention(extension.getRunDirectory().file(DEFAULT_KNOWN_HOSTS_FILE_NAME));
                    task.getDomainOperations().set(domainOperations);
                });

        var readKubeConfig = project.getTasks().register(
                READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> {
                    task.getDomainName().convention(extension.getDomain().getName());
                    task.getKubeConfigFile().convention(extension.getRunDirectory().file(DEFAULT_KUBE_CONFIG_FILE_NAME));
                    task.getKnownHostsFile().convention(startDomain.get().getKnownHostsFile());
                    task.getSecureShellService().set(secureShellService);
                });

        project.getTasks().register(
                STATUS_TASK_NAME,
                StatusTask.class,
                task -> {
                    task.getPoolOperations().set(poolOperations);
                    task.getDomainOperations().set(domainOperations);
                    task.getRegistryService().set(registryService);
                    task.getSecureShellService().set(secureShellService);
                    task.getDomainName().convention(extension.getDomain().getName());
                    task.getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
                    task.getKnownHostsFile().convention(startDomain.get().getKnownHostsFile());
                    task.getDistributionName().convention(extension.getBaseImage().getName());
                    task.getDownloadDir().convention(extension.getBaseImage().getDownloadDir());
                    task.getDistributionDir().convention(extension.getBaseImage().getDistributionDir());
                    task.getBaseImageFile().convention(extension.getBaseImage().getBaseImageFile());
                });

        project.getTasks().withType(Transform.class).configureEach(
                task -> task.getScope().convention(extension.asPropertyMap(project.getObjects())));

        project.getTasks().register(
                START_TASK_NAME,
                DefaultTask.class,
                task -> task.dependsOn(readKubeConfig));

        project.getTasks().register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> {
                    task.getPoolOperations().set(poolOperations);
                    task.getDomain().convention(extension.getDomain().getName());
                    task.getPoolRunFile().convention(createPool.get().getPoolRunFile());
                    task.getBuildDir().convention(project.getLayout().getBuildDirectory());
                    task.getDomainOperations().set(domainOperations);
                });
    }

    private static String templateFilename(String filename) {
        return String.format("%s.%s", filename, TEMPLATE_FILENAME_EXTENSION);
    }
}
