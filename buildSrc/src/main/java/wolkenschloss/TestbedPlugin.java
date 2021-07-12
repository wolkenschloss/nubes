package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import wolkenschloss.remote.SecureShellService;
import wolkenschloss.status.RegistryService;
import wolkenschloss.status.Status;
import wolkenschloss.transformation.Transform;

@SuppressWarnings("UnstableApiUsage")
public class TestbedPlugin implements Plugin<Project> {


    public static final String READ_KUBE_CONFIG_TASK_NAME = "readKubeConfig";
    public static final String STATUS_TASK_NAME = "status";
    public static final String START_TASK_NAME = "start";
    public static final String DESTROY_TASK_NAME = "destroy";

    public static final String TESTBED_EXTENSION_NAME = "testbed";

    public static final String DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig";

    public static final String DEFAULT_KNOWN_HOSTS_FILE_NAME = "known_hosts";



    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create(TESTBED_EXTENSION_NAME, TestbedExtension.class)
                .configure(project, project.getLayout());

        var sharedServices = project.getGradle().getSharedServices();



        var secureShellService = sharedServices.registerIfAbsent(
                "sshservice",
                SecureShellService.class,
                spec -> {
                    var parameters = spec.getParameters();
                    parameters.getDomainOperations().set(extension.getDomainOperations());
                    parameters.getKnownHostsFile().set(extension.getRunDirectory().file(DEFAULT_KNOWN_HOSTS_FILE_NAME));
                });



        var registryService = sharedServices.registerIfAbsent(
                "registryService",
                RegistryService.class,
                spec -> spec.getParameters().getDomainOperations().set(extension.getDomainOperations()));

        var registrar = new Registrar(project, extension);

        var buildDataSourceImage = registrar.getBuildDataSourceImageTaskProvider();

        var buildRootImage = registrar.getBuildRootImageTaskProvider();

        var buildPool = registrar.getBuildPoolTaskProvider(buildDataSourceImage, buildRootImage);

        var buildDomain = registrar.getBuildDomainTaskProvider(extension.getDomainOperations(), buildPool);

        var readKubeConfig = project.getTasks().register(
                READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> {
                    task.getDomainName().convention(extension.getDomain().getName());
                    task.getKubeConfigFile().convention(extension.getRunDirectory().file(DEFAULT_KUBE_CONFIG_FILE_NAME));
                    task.getKnownHostsFile().convention(buildDomain.get().getKnownHostsFile());
                    task.getSecureShellService().set(secureShellService);
                });

        project.getTasks().register(
                STATUS_TASK_NAME,
                Status.class,
                task -> {
                    task.getPoolOperations().set(extension.getPoolOperations());
                    task.getDomainOperations().set(extension.getDomainOperations());
                    task.getRegistryService().set(registryService);
                    task.getSecureShellService().set(secureShellService);
                    task.getDomainName().convention(extension.getDomain().getName());
                    task.getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
                    task.getKnownHostsFile().convention(buildDomain.get().getKnownHostsFile());
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
                task -> {
                    task.setGroup(Registrar.BUILD_GROUP_NAME);
                    task.dependsOn(readKubeConfig);
                });

        project.getTasks().register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> {
                    task.setGroup(Registrar.BUILD_GROUP_NAME);
                    task.getPoolOperations().set(extension.getPoolOperations());
                    task.getDomain().convention(extension.getDomain().getName());
                    task.getPoolRunFile().convention(buildPool.get().getPoolRunFile());
                    task.getBuildDir().convention(project.getLayout().getBuildDirectory());
                    task.getDomainOperations().set(extension.getDomainOperations());
                });
    }

}
