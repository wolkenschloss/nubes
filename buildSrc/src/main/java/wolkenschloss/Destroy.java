package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.Destroys;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.libvirt.LibvirtException;
import wolkenschloss.domain.DomainOperations;
import wolkenschloss.pool.PoolOperations;

import javax.inject.Inject;
import java.util.UUID;

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

    @Internal
    abstract public Property<DomainOperations> getDomainOperations();

    @Inject
    abstract public ProviderFactory getProviderFactory();

    @TaskAction
    public void destroy() throws LibvirtException {
        destroyDomain();
        destroyPool();
        deleteBuildDirectory();
    }

    private void destroyDomain() throws LibvirtException {
        var domainOperations = getDomainOperations().get();
        var deleted = domainOperations.deleteDomainIfExists(getDomain());
        if (deleted) {
            getLogger().info("Domain deleted.");
        }
    }

    private void destroyPool() throws LibvirtException {
        if (getPoolRunFile().get().getAsFile().exists()) {
            var content = getProviderFactory().fileContents(getPoolRunFile());

            @SuppressWarnings("NullableProblems")
            var uuid = content.getAsText().map(UUID::fromString).get();

            getPoolOperations().get().destroy(uuid);
            getFileSystemOperations().delete(f -> f.delete(getPoolRunFile()));
        }
    }

    private void deleteBuildDirectory() {
        getFileSystemOperations().delete(
                spec -> spec.delete(getBuildDir()));
    }
}
