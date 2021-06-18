package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.*;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestbedPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        var extension = project.getExtensions()
                .create("testbed", TestbedExtension.class);

        extension.getSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile());
        extension.getView().getUser().convention(System.getenv("USER"));
        extension.getView().getHostname().convention("testbed");
        extension.getView().getSshKey().convention(extension.getSshKeyFile().map(this::readSshKey));

        var transform = project.getTasks().register("transform", DefaultTask.class, task -> {
            task.setDescription("Transforms all templates");
        });

//        project.getTasks().register("hello", TransformerTask.class, task -> {
//            task.getView().set(extension.getView());
//            task.getSshKeyFile().set(extension.getSshKeyFile());
//            task.getOutputDir().set(project.getLayout().getBuildDirectory().dir("config"));
//
//        });

        project.getTasks().register("clean", Delete.class, task -> {
            task.getDelete().add(task.getProject().getLayout().getBuildDirectory());
        });


        var partialPathBuilder = new PartialPathBuilder(project.getLayout());

        var src = project.getLayout().getProjectDirectory().dir("src");
        var cloudInitDir = project.getLayout().getBuildDirectory().dir("cloud-init").get();
        var poolDir = project.getLayout().getBuildDirectory().dir("pool");

        var networkConfig = createTransformationTask(project,extension, "CloudInit",
                src.file("network-config.mustache"),
                cloudInitDir.file("network-config"));

        var userData = createTransformationTask(project,extension, "UserData",
                src.file("user-data.mustache"),
                cloudInitDir.file("user-data"));

        transform.configure(t -> t.dependsOn(networkConfig.get(), userData.get()));

        project.getTasks().register("cidata", CloudLocalDs.class, task -> {
            task.getCidata().convention(poolDir.get().file("cidata.img"));
            task.getNetworkConfig().convention(networkConfig.get().getOutputFile());
            task.getUserData().convention(userData.get().getOutputFile());
        });

        project.getTasks().register("root", RootImageTask.class, task -> {

            task.getRootImage().convention(poolDir.get().file("root.qcow2"));
            // Provisorisch
            task.getBaseImage().set(project.getLayout()
                    .getProjectDirectory()
                    .dir(".cache")
                    .file("focal-server-cloudimg-amd64-disk-kvm.img"));
        });

        //        var pool = createTransformationTask(project, extension, "Pool", poolPath);
//        var domain = createTransformationTask(project, extension, "Domain", domainPath);


    }

    private <T extends FileSystemLocation> TaskProvider<TransformerTask> createTransformationTask(
            Project project,
            TestbedExtension extension,
            String name,
            RegularFile template,
            RegularFile output) {
        return project.getTasks().register("transform" + name, TransformerTask.class, task -> {
            task.getView().set(extension.getView());
            task.getSshKeyFile().set(extension.getSshKeyFile());

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
