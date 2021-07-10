package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Destroys;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import wolkenschloss.model.Testbed;

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
