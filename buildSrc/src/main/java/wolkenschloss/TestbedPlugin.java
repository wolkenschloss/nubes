package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.internal.provider.Providers;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.TaskProvider;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestbedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {

        var extension = project.getObjects().newInstance(TestbedExtension.class);
        project.getExtensions().add("testbed", extension);

        var src = project.getLayout().getProjectDirectory().dir("src");
        var cloudInitDir = project.getLayout().getBuildDirectory().dir("cloud-init").get();
        var poolDir = project.getLayout().getBuildDirectory().dir("pool");
        var virshConfigDir = project.getLayout().getBuildDirectory().dir("config");

        extension.getPoolDirectory().set(project.getLayout().getBuildDirectory().dir("pool"));

        extension.getSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile());
        extension.getView().getUser().convention(System.getenv("USER"));
        extension.getView().getHostname().convention("testbed");
        extension.getView().getSshKey().convention(extension.getSshKeyFile().map(this::readSshKey));
        extension.getView().getLocale().convention(System.getenv("LANG"));
        extension.getPoolDirectory().set(poolDir);
        extension.getCloudInitDirectory().set(cloudInitDir);
        extension.getConfigDirectory().set(virshConfigDir);
        extension.getRootImageName().convention("root.qcow2");
        extension.getCidataImageName().convention("cidata.img");


        extension.getPool().getName().convention("testbed");
        extension.getPool().getPath().convention(project.getLayout().getBuildDirectory().dir("xppoollx").get().getAsFile().getAbsolutePath());


        try {
            InetAddress host = InetAddress.getLocalHost();
            extension.getView().getHostAddress().set(host.getHostName());
        } catch (UnknownHostException e) {
            throw new GradleScriptException("Can not get host Address", e);
        }

        var transform = project.getTasks().register("transform", DefaultTask.class, task -> {
            task.setDescription("Transforms all templates");
        });

        extension.getView().getCallbackPort().set(9191);

//        project.getTasks().register("clean", Delete.class, task -> {
//            task.getDelete().add(task.getProject().getLayout().getBuildDirectory());
//        });

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
            try {
//            URL location = new URL("file:///home/administrator/.local/src/mycloud/testbed/.cache/focal-server-cloudimg-amd64-disk-kvm.img");
//            URL location = new URL("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img");

                var location = new URL("file:///home/administrator/.local/src/mycloud/testbed/.cache/focal-server-cloudimg-amd64-disk-kvm.img");
                task.getBaseImageUrl().convention(new Providers.FixedValueWithChangingContentProvider<>(location));
            } catch (MalformedURLException e) {
                throw new GradleScriptException("Die URL ist kaputt.", e);
            }

            task.getSha256Sum().set("73e8c576d0ad02f1cf393c664856bce4146d91affa0062e0d4fedaca55163e44");
            task.getBaseImage().set(poolDir.get().file("base.img"));
        });

        var root = project.getTasks().register("root", RootImageTask.class, task -> {
            task.getRootImage().convention(poolDir.get().file(extension.getRootImageName()));
            task.getBaseImage().convention(download.get().getBaseImage());
        });

        var resize = project.getTasks().register("resize", ResizeTask.class, task-> {
            task.getSize().convention("20G");
            task.getImage().set(poolDir.get().file("root.qcow2"));
            task.dependsOn(root);
        });

        var poolConfig = createTransformationTask(project, extension, "Pool",
                src.file("pool.xml.mustache"),
                virshConfigDir.get().file("pool.xml"));

        var domainConfig = createTransformationTask(project, extension, "Domain",
                src.file("domain.xml.mustache"),
                virshConfigDir.get().file("domain.xml"));

        transform.configure(t -> t.dependsOn(
                networkConfig.get(),
                userData.get(),
                poolConfig.get(),
                domainConfig.get()
        ));

        project.getTasks().register("start", DefaultTask.class, task -> {
            task.dependsOn(transform, cidata, resize);
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

    private String readSshKey(RegularFile sshKeyFile) {

        var file = sshKeyFile.getAsFile();

        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new GradleScriptException("Can not read public ssh key", e);
        }
    }
}