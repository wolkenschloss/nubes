package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.domain.BuildDomain;
import wolkenschloss.domain.DomainExtension;
import wolkenschloss.pool.*;
import wolkenschloss.status.Status;
import wolkenschloss.transformation.Transform;
import wolkenschloss.transformation.TransformationTasksRegistrar;
import wolkenschloss.download.DownloadTasksRegistrar;
import wolkenschloss.download.BaseImageExtension;
import wolkenschloss.download.DownloadDistribution;

public class Registrar {
    public static final String BUILD_GROUP_NAME = "build";

    public static final String BUILD_DATA_SOURCE_IMAGE_TASK_NAME = "buildDataSourceImage";

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
        var transformationTasks = new TransformationTasksRegistrar(project, BUILD_GROUP_NAME);
        transformationTasks.registerTransformationTasks(extension.getTransformation());

        TaskContainer tasks = getProject().getTasks();

        BaseImageExtension baseImage = getExtension().getBaseImage();
        var downloadTasks = new DownloadTasksRegistrar(tasks);
        downloadTasks.register(baseImage);

        PoolExtension pool = getExtension().getPool();
        PoolTasks poolTasks = new PoolTasks(pool);
        poolTasks.registerBuildDataSourceImageTask(tasks, BUILD_GROUP_NAME);
        PoolTasks.registerBuildRootImageTask(tasks, pool);
        PoolTasks.registerBuildPoolTask(tasks, pool);

        DomainExtension domain = getExtension().getDomain();
        HostExtension host = getExtension().getHost();
        Provider<RegularFile> kubeConfig = getExtension().getRunDirectory().file(DEFAULT_KUBE_CONFIG_FILE_NAME);

        registerBuildDomainTask(tasks, domain, host);
        registerReadKubeConfig(tasks, domain, kubeConfig);

        registerStatusTask(tasks, domain, pool);

        getProject().getTasks().withType(Transform.class).configureEach(
                task -> task.getScope().convention(getExtension().asPropertyMap(getProject().getObjects())));

        getProject().getTasks().register(
                START_TASK_NAME,
                DefaultTask.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.dependsOn(tasks.named(READ_KUBE_CONFIG_TASK_NAME));
                });

        registerDestroyTask(getProject().getTasks());
    }

    private void registerDestroyTask(TaskContainer tasks) {
        var poolRunFile = tasks.named(BUILD_POOL_TASK_NAME, BuildPool.class)
                .map(t -> t.getPoolRunFile().get());

        tasks.register(
                DESTROY_TASK_NAME,
                Destroy.class,
                task -> {
                    task.setGroup(BUILD_GROUP_NAME);
                    task.getPoolOperations().set(getExtension().getPool().getPoolOperations());
                    task.getDomain().convention(getExtension().getDomain().getName());
                    task.getPoolRunFile().convention(poolRunFile);
                    task.getBuildDir().convention(getProject().getLayout().getBuildDirectory());
                    task.getDomainOperations().set(getExtension().getDomain().getDomainOperations());
                });
    }

    private void registerReadKubeConfig(TaskContainer tasks, DomainExtension domain, Provider<RegularFile> kubeConfig) {
        var knownHostsFile = tasks.named(BUILD_DOMAIN_TASK_NAME, BuildDomain.class)
                .map(BuildDomain::getKnownHostsFile)
                .get();

        tasks.register(
                READ_KUBE_CONFIG_TASK_NAME,
                CopyKubeConfig.class,
                task -> {
                    task.getDomainName().convention(domain.getName());
                    task.getKubeConfigFile().convention(kubeConfig);
                    task.getKnownHostsFile().convention(knownHostsFile);
                    task.getDomainOperations().set(domain.getDomainOperations());
                });
    }

    public void registerStatusTask(TaskContainer tasks, DomainExtension domain, PoolExtension pool) {

        var readKubeConfig = tasks.named(READ_KUBE_CONFIG_TASK_NAME, CopyKubeConfig.class);

        var knownHostsFile = tasks.named(BUILD_DOMAIN_TASK_NAME, BuildDomain.class)
                .map(BuildDomain::getKnownHostsFile)
                .get();

        var downloadDistribution = tasks.named(
                DownloadTasksRegistrar.DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class);

        var distributionDir = downloadDistribution.map(d -> d.getDistributionDir().get());

        tasks.register(
                STATUS_TASK_NAME,
                Status.class,
                task -> {
                    task.getPoolOperations().set(pool.getPoolOperations());
                    task.getDomainOperations().set(domain.getDomainOperations());
                    task.getDomainName().convention(domain.getName());
                    task.getKubeConfigFile().convention(readKubeConfig.get().getKubeConfigFile());
                    task.getKnownHostsFile().convention(knownHostsFile);
                    task.getDownloadDir().convention(distributionDir);
                    task.getBaseImageFile().convention(downloadDistribution.map(d -> d.getBaseImage().get()));
                });
    }


    public static void registerBuildDomainTask(TaskContainer tasks, DomainExtension domain, HostExtension host) {
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
                    task.getKnownHostsFile().convention(domain.getKnownHostsFile());
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
