package wolkenschloss;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.nio.file.Path;

public abstract class BaseImageExtension {

    @Inject
    public abstract ObjectFactory getObjects();

    public abstract Property<String> getUrl();

    public abstract Property<String> getName();

    public abstract DirectoryProperty getDownloadDir();

    public abstract DirectoryProperty getDistributionDir();

    public abstract RegularFileProperty getBaseImageFile();

    public void initialize() {
        getUrl().convention("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img");
        getName().convention("ubuntu-20.04");

        var xdgDataHome = getXdgDataHome().resolve("testbed");

        getDownloadDir().set(xdgDataHome.toFile());

        getDistributionDir().convention(getDownloadDir().dir(getName()));
        var parts = getUrl().get().split("/");
        var basename = parts[parts.length - 1];

        getBaseImageFile().convention(getDistributionDir().file(basename));
    }

    private Path getXdgDataHome() {
        var dataDir = System.getenv().get("XDG_DATA_HOME");
        if (dataDir == null) {
            return Path.of(System.getenv("HOME"), ".local", "share");
        } else {
            return Path.of(dataDir);
        }
    }
}
