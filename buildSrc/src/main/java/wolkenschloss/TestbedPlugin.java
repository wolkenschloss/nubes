package wolkenschloss;

import org.gradle.api.*;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import wolkenschloss.task.*;
import wolkenschloss.task.status.StatusTask;
import wolkenschloss.task.start.Start;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestbedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        TestbedExtension extension = project.getExtensions()
                .create("testbed", TestbedExtension.class);

        var distribution = new Distribution(project.getObjects(), extension.getBaseImage().getName());

        // Set build directories
        var buildDirectory = project.getLayout().getBuildDirectory();
        extension.getPoolDirectory().set(buildDirectory.dir("pool"));
        extension.getRunDirectory().set(buildDirectory.dir("run"));
        extension.getGeneratedCloudInitDirectory().set(buildDirectory.dir("cloud-init"));
        extension.getGeneratedVirshConfigDirectory().set(buildDirectory.dir("config"));

        extension.getSourceDirectory().set(project.getLayout().getProjectDirectory().dir("src"));

        extension.getSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile());
        extension.getView().getUser().convention(System.getenv("USER"));
        extension.getView().getHostname().convention("testbed");
        extension.getView().getFqdn().convention("testbed.wolkenschloss.local");
        extension.getView().getSshKey().convention(extension.getSshKeyFile().map(this::readSshKey));
        extension.getView().getLocale().convention(System.getenv("LANG"));
        extension.getView().getHostAddress().convention(IpUtil.getHostAddress());

        extension.getRootImageName().convention("root.qcow2");
        extension.getCidataImageName().convention("cidata.img");

        extension.getPool().getName().convention("testbed");

        extension.getView().getCallbackPort().set(9191);

        extension.getBaseImage().getUrl().convention("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img");
        extension.getBaseImage().getName().convention("ubuntu-20.04");

        var networkConfig = createTransformationTask(project, "CloudInit",
                extension.getSourceDirectory().file("network-config.mustache"),
                extension.getGeneratedCloudInitDirectory().file("network-config"));

        var userData = createTransformationTask(project, "UserData",
                extension.getSourceDirectory().file("user-data.mustache"),
                extension.getGeneratedCloudInitDirectory().file("user-data"));

        var cidata = project.getTasks().register("cidata", CreateDataSource.class, task -> {
            // InputFiles
            task.getNetworkConfig().convention(networkConfig.get().getOutputFile());
            task.getUserData().convention(userData.get().getOutputFile());

            // OutputFile
            task.getCidata().convention(extension.getPoolDirectory().file(extension.getCidataImageName()));
        });

        var download = project.getTasks().register("download", Download.class, task -> {
            task.getBaseImageLocation().convention(extension.getBaseImage().getUrl());
            task.getDistributionName().convention(extension.getBaseImage().getName());
            var parts = extension.getBaseImage().getUrl().get().split("/");
            var basename = parts[parts.length - 1];
            task.getBaseImage().convention(distribution.file(basename));
        });

        var root = project.getTasks().register("root", CreateRootImage.class, task -> {
            task.getSize().convention("20G");
            task.getBaseImage().convention(download.get().getBaseImage());
            task.getRootImage().convention(extension.getPoolDirectory().file(extension.getRootImageName()));
            task.getRootImageMd5File().set(extension.getRunDirectory().file("root.md5"));
        });

        var poolConfig = createTransformationTask(project, "Pool",
                extension.getSourceDirectory().file("pool.xml.mustache"),
                extension.getGeneratedVirshConfigDirectory().file("pool.xml"));

        var domainConfig = createTransformationTask(project, "Domain",
                extension.getSourceDirectory().file("domain.xml.mustache"),
                extension.getGeneratedVirshConfigDirectory().file("domain.xml"));

        var createPool = project.getTasks().register("createPool", CreatePool.class, task -> {
            task.getPoolName().set(extension.getPool().getName());
            task.getXmlDescription().set(poolConfig.get().getOutputFile());
            task.getPoolRunFile().set(extension.getRunDirectory().file("pool.run"));
            task.getDomainName().set(extension.getView().getHostname());
            task.dependsOn(root, cidata);
        });

        var startDomain = project.getTasks().register("startDomain", Start.class, task -> {
            // TODO: Refactor. testbed darf nur ein einziges mal erscheinen.
            task.dependsOn(createPool);
            task.getDomain().set(extension.getView().getHostname());
            task.getHostname().set(extension.getView().getHostname());
            task.getPort().set(extension.getView().getCallbackPort());
            task.getXmlDescription().set(domainConfig.get().getOutputFile());
            task.getPoolRunFile().set(createPool.get().getPoolRunFile());
            task.getKnownHostsFile().set(extension.getRunDirectory().file("known_hosts"));
        });

        var readKubeConfig = project.getTasks().register("readKubeConfig", CopyKubeConfig.class, task -> {
            // benötigt funktionierenden ssh Zugang. Deswegen muss updateKnownHosts
            // vorher ausgeführt sein.
//            task.dependsOn(updateKnownHosts);
            task.getDomainName().set(extension.getView().getHostname());
            task.getKubeConfigFile().convention(extension.getRunDirectory().file("kubeconfig"));
            task.getKnownHostsFile().set(startDomain.get().getKnownHostsFile());
        });

        var status = project.getTasks().register("status", StatusTask.class, task -> {
            task.getDomainName().set(extension.getView().getHostname());
            task.getKubeConfigFile().set(readKubeConfig.get().getKubeConfigFile());
            task.getKnownHostsFile().set(startDomain.get().getKnownHostsFile());
            task.getPoolName().set(extension.getPool().getName());
            task.getDistributionName().set(extension.getBaseImage().getName());
        });

        project.getTasks().withType(Transform.class).configureEach(
                configureTransformTask(extension, project.getObjects()));

        project.getTasks().register("start", DefaultTask.class,
                task -> task.dependsOn(readKubeConfig));

        project.getTasks().register("destroy", Destroy.class, task -> {
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
            task.getTemplate().set(template);
            task.getOutputFile().set(output);
        });
    }


    @Nonnull
    private String readSshKey(RegularFile sshKeyFile) {

        var file = sshKeyFile.getAsFile();

        try {
            return Files.readString(file.toPath()).trim();
        } catch (IOException e) {
            throw new GradleScriptException("Can not read public ssh key", e);
        }
    }
}
