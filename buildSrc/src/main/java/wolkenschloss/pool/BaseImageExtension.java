package wolkenschloss.pool;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.nio.file.Path;

public abstract class BaseImageExtension {

    public static final String DEFAULT_DOWNLOAD_URL = "https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img";
    public static final String DEFAULT_DISTRIBUTION_NAME = "ubuntu-20.04";
    public static final String APP_NAME = "testbed";

    @Inject
    public abstract ObjectFactory getObjects();

    public abstract Property<String> getUrl();

    public abstract Property<String> getName();

    public abstract DirectoryProperty getDownloadDir();

    public abstract DirectoryProperty getDistributionDir();

    public abstract RegularFileProperty getBaseImageFile();

    public void initialize() {
        getUrl().convention(DEFAULT_DOWNLOAD_URL);
        getName().convention(DEFAULT_DISTRIBUTION_NAME);

        var xdgDataHome = getXdgDataHome().resolve(APP_NAME);

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
