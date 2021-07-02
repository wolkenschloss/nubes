package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.tasks.TaskProvider;
import wolkenschloss.task.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestbedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        BaseTestbedExtension extension = project.getObjects().newInstance(TestbedExtension.class);
        project.getExtensions().add("testbed", extension);

        var src = project.getLayout().getProjectDirectory().dir("src");
        var runDir = project.getLayout().getBuildDirectory().dir("run");
        var cloudInitDir = project.getLayout().getBuildDirectory().dir("cloud-init").get();
        var poolDir = project.getLayout().getBuildDirectory().dir("pool");
        var virshConfigDir = project.getLayout().getBuildDirectory().dir("config");

        var distribution = new Distribution(project.getObjects(), extension.getBaseImage().getName());

        extension.getPoolDirectory().set(poolDir);

        extension.getSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile());
        extension.getView().getUser().convention(System.getenv("USER"));
        extension.getView().getHostname().convention("testbed");
        extension.getView().getFqdn().convention("testbed.wolkenschloss.local");
        extension.getView().getSshKey().convention(extension.getSshKeyFile().map(this::readSshKey));
        extension.getView().getLocale().convention(System.getenv("LANG"));
        extension.getView().getHostAddress().convention(IpUtil.getHostAddress());

        extension.getCloudInitDirectory().set(cloudInitDir);
        extension.getConfigDirectory().set(virshConfigDir);
        extension.getRootImageName().convention("root.qcow2");
        extension.getCidataImageName().convention("cidata.img");


        extension.getPool().getName().convention("testbed");
        extension.getPool().getPath().convention(poolDir.get().getAsFile().getAbsolutePath());

        var transform = project.getTasks().register("transform", DefaultTask.class,
                task -> task.setDescription("Transforms all templates"));

        extension.getView().getCallbackPort().set(9191);

        project.getTasks().register("clean", TestbedCleanTask.class, task -> {
        });

        var networkConfig = createTransformationTask(project,extension, "CloudInit",
                src.file("network-config.mustache"),
                cloudInitDir.file("network-config"));

        var userData = createTransformationTask(project,extension, "UserData",
                src.file("user-data.mustache"),
                cloudInitDir.file("user-data"));

        var cidata = project.getTasks().register("cidata", CloudLocalDsTask.class, task -> {
            task.getCidata().convention(poolDir.get().file(extension.getCidataImageName()));
            task.getNetworkConfig().convention(networkConfig.get().getOutputFile());
            task.getUserData().convention(userData.get().getOutputFile());
        });

        var download = project.getTasks().register("download", DownloadTask.class, task -> {
            task.getBaseImageLocation().convention(extension.getBaseImage().getUrl());
            task.getDistributionName().convention(extension.getBaseImage().getName());
            var parts = extension.getBaseImage().getUrl().get().split("/");
            var basename = parts[parts.length - 1];
            task.getBaseImage().convention(distribution.file(basename));
        });

        var root = project.getTasks().register("root", RootImageTask.class, task -> {
            task.getSize().convention("20G");
            task.getBaseImage().convention(download.get().getBaseImage());
            task.getRootImage().convention(poolDir.get().file(extension.getRootImageName()));
            task.getRootImageMd5File().set(runDir.get().file("root.md5"));
        });

        var poolConfig = createTransformationTask(project, extension, "Pool",
                src.file("pool.xml.mustache"),
                virshConfigDir.get().file("pool.xml"));

        var domainConfig = createTransformationTask(project, extension, "Domain",
                src.file("domain.xml.mustache"),
                virshConfigDir.get().file("domain.xml"));

        var createPool = project.getTasks().register("createPool", CreatePoolTask.class, task -> {
            task.getPoolName().set(extension.getPool().getName());
            task.getXmlDescription().set(poolConfig.get().getOutputFile());
            task.getPoolRunFile().set(runDir.get().file("pool.run"));
            task.getDomainName().set(extension.getView().getHostname());
            task.dependsOn(root, cidata);
        });

        var startDomain = project.getTasks().register("startDomain", StartDomainTask.class, task -> {
            // TODO: Refactor. testbed darf nur ein einziges mal erscheinen.
            task.dependsOn(createPool);
            task.getDomain().set(extension.getView().getHostname());
            task.getHostname().set(extension.getView().getHostname());
            task.getPort().set(extension.getView().getCallbackPort());
            task.getXmlDescription().set(domainConfig.get().getOutputFile());
            task.getPoolRunFile().set(createPool.get().getPoolRunFile());
            task.getKnownHostsFile().set(runDir.get().file("known_hosts"));
        });

        var readKubeConfig = project.getTasks().register("readKubeConfig", ReadKubeConfigTask.class, task -> {
            // benötigt funktionierenden ssh Zugang. Deswegen muss updateKnownHosts
            // vorher ausgeführt sein.
//            task.dependsOn(updateKnownHosts);
            task.getDomainName().set(extension.getView().getHostname());
            task.getKubeConfigFile().convention(runDir.get().file("kubeconfig"));
            task.getKnownHostsFile().set(startDomain.get().getKnownHostsFile());
        });

        var status = project.getTasks().register("status", StatusTask.class, task -> {
            task.getDomainName().set(extension.getView().getHostname());
            task.getKubeConfigFile().set(readKubeConfig.get().getKubeConfigFile());
            task.getKnownHostsFile().set(startDomain.get().getKnownHostsFile());
            task.getPoolName().set(extension.getPool().getName());
            task.getDistributionName().set(extension.getBaseImage().getName());
        });

        transform.configure(t -> t.dependsOn(
                networkConfig.get(),
                userData.get(),
                poolConfig.get(),
                domainConfig.get()
        ));

        project.getTasks().register("start", DefaultTask.class,
                task -> task.dependsOn(readKubeConfig));

        project.getTasks().register("destroy", DestroyTask.class, task -> {
            task.getDomain().set(extension.getView().getHostname());
            task.getKubeConfigFile().set(readKubeConfig.get().getKubeConfigFile());
            task.getKnownHostsFile().set(startDomain.get().getKnownHostsFile());
            task.getDomainXmlConfig().set(domainConfig.get().getOutputFile());
            task.getPoolRunFile().set(createPool.get().getPoolRunFile());
            task.getPoolXmlConfig().set(poolConfig.get().getOutputFile());
            task.getRootImageFile().set(root.get().getRootImage());
            task.getRootImageMd5File().set(root.get().getRootImageMd5File());
            task.getCiDataImageFile().set(cidata.get().getCidata());
            task.getNetworkConfig().set(networkConfig.get().getOutputFile());
            task.getUserData().set(userData.get().getOutputFile());
        });
    }

    private <T extends FileSystemLocation> TaskProvider<TransformerTask> createTransformationTask(
            Project project,
            BaseTestbedExtension extension,
            String name,
            RegularFile template,
            RegularFile output) {
        return project.getTasks().register("transform" + name, TransformerTask.class, task -> {
            task.getRootImageName().set(extension.getRootImageName());
            task.getCidataImageName().set(extension.getCidataImageName());
            task.getView().set(extension.getView());
            task.getPool().set(extension.getPool());
            task.getTemplate().set(template);
            task.getOutputFile().set(output);
        });
    }


    @Nonnull
    private String readSshKey(RegularFile sshKeyFile) {

        var file = sshKeyFile.getAsFile();

        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new GradleScriptException("Can not read public ssh key", e);
        }
    }
}
