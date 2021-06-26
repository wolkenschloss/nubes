package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Destroys;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public abstract class DestroyTask extends DefaultTask {

    @Destroys
    abstract public RegularFileProperty getKubeConfigFile();

    @Destroys
    abstract public RegularFileProperty getKnownHostsFile();

    @Destroys
    abstract public RegularFileProperty getPoolRunFile();

    @Destroys
    abstract public RegularFileProperty getPoolXmlConfig();

    @Destroys
    abstract public RegularFileProperty getRootImageMd5File();

    @Destroys
    abstract public RegularFileProperty getRootImageFile();

    @Destroys
    abstract public DirectoryProperty getDownloads();

    @Destroys
    abstract public RegularFileProperty getCiDataImageFile();

    @Destroys
    abstract public RegularFileProperty getDomainXmlConfig();

    @Destroys
    abstract public RegularFileProperty getNetworkConfig();

    @Destroys
    abstract public RegularFileProperty getUserData();

    @Internal
    abstract public Property<String> getDomain();

    @TaskAction
    public void destroy() throws Exception {

        // ReadKubeConfigTask
        Files.deleteIfExists(getKubeConfigFile().getAsFile().get().toPath());

        // StartDomainTask
        Files.deleteIfExists(getKnownHostsFile().getAsFile().get().toPath());

        try (var testbed = new Testbed()) {
            var deleted = testbed.deleteDomainIfExists(getDomain());
            if (deleted) {
                getLogger().info("Domain deleted.");
            }
        }

        Files.deleteIfExists(getDomainXmlConfig().getAsFile().get().toPath());

        // CreatePoolTask
        try (var testbed = new Testbed()) {
            testbed.deletePoolIfExists(getPoolRunFile());
        }

        Files.deleteIfExists(getPoolXmlConfig().getAsFile().get().toPath());

        // RootImageTask
        Files.deleteIfExists(getRootImageMd5File().getAsFile().get().toPath());
        Files.deleteIfExists(getRootImageFile().getAsFile().get().toPath());

        // Download
        for (Path path : Files.list(getDownloads().getAsFile().get().toPath()).collect(Collectors.toList())) {
            Files.deleteIfExists(path);
        }

        Files.deleteIfExists(getDownloads().getAsFile().get().toPath());

        // CiDataTask
        Files.deleteIfExists(getCiDataImageFile().getAsFile().get().toPath());
        Files.deleteIfExists(getNetworkConfig().getAsFile().get().toPath());
        Files.deleteIfExists(getUserData().getAsFile().get().toPath());
    }
}
