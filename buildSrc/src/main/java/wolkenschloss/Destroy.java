package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Destroys;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import wolkenschloss.pool.PoolOperations;

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

    @Internal
    abstract public Property<PoolOperations> getPoolOperations();

    @TaskAction
    public void destroy() throws Exception {

        try (var testbed = new Testbed(getDomain().get())) {
            var deleted = testbed.deleteDomainIfExists(getDomain());
            if (deleted) {
                getLogger().info("Domain deleted.");
            }

            getPoolOperations().get().deletePoolIfExists(getPoolRunFile());
        }

        getFileSystemOperations().delete(spec -> {
            spec.delete(getBuildDir());
        });
    }
}
