package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.*;
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

        project.getTasks().register("hello", TransformerTask.class, task -> {
            task.getView().set(extension.getView());
            task.getSshKeyFile().set(extension.getSshKeyFile());
            task.getOutputDir().set(project.getLayout().getBuildDirectory().dir("config"));

        });


        var partialPathBuilder = new PartialPathBuilder(project.getLayout());

        var cloudInitPath = partialPathBuilder.dir("cloud-init");
        var poolPath = partialPathBuilder.file("virsh/pool/pool.xml.mustache");
        var domainPath = partialPathBuilder.file("virsh/domain.xml");
        var dashboardIngressPath = partialPathBuilder.file("kubernetes/dashboard.xml");

        var cloudinit = createTransformationTask(project,extension, "CloudInit", cloudInitPath);

        var pool = createTransformationTask(project, extension, "Pool", poolPath);
        transform.configure(t -> t.dependsOn(cloudinit.get(), pool.get()));
    }

    private <T extends FileSystemLocation> TaskProvider<TransformerTask> createTransformationTask(
            Project project,
            TestbedExtension extension,
            String cloudInit, PartialPath<T> cloudInitPath) {
        return project.getTasks().register("transform" + cloudInit, TransformerTask.class, task -> {
            task.getView().set(extension.getView());
            task.getSshKeyFile().set(extension.getSshKeyFile());

            task.getTemplates().set(cloudInitPath.source());
            task.getOutputDir().set(cloudInitPath.build());
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
