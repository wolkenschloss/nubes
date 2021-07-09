package wolkenschloss.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Destroys;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskProvider;

// TODO: Refactor
import wolkenschloss.Testbed;
import wolkenschloss.TestbedExtension;

import javax.inject.Inject;

public abstract class Destroy extends DefaultTask {

    @Destroys
    abstract public RegularFileProperty getPoolRunFile();

    @Destroys
    abstract public DirectoryProperty getBuildDir();

    @Internal
    abstract public Property<String> getDomain();

    @Inject
    abstract public FileSystemOperations getFileSystemOperations();

    public void initialize(Project project, TestbedExtension extension, TaskProvider<CreatePool> createPool) {
        getDomain().convention(extension.getDomain().getName());
        getPoolRunFile().convention(createPool.get().getPoolRunFile());
        getBuildDir().convention(project.getLayout().getBuildDirectory());
    }

    @TaskAction
    public void destroy() throws Exception {

        try (var testbed = new Testbed(getDomain().get())) {
            var deleted = testbed.deleteDomainIfExists(getDomain());
            if (deleted) {
                getLogger().info("Domain deleted.");
            }

            testbed.deletePoolIfExists(getPoolRunFile());
        }

        getFileSystemOperations().delete(spec -> {
            spec.delete(getBuildDir());
        });
    }
}
