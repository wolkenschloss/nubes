package wolkenschloss.download;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import wolkenschloss.Directories;

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

    public void initialize(Project project) {
        getUrl().convention(DEFAULT_DOWNLOAD_URL);
        getName().convention(DEFAULT_DISTRIBUTION_NAME);

        getDownloadDir().set(project.getLayout()
                .getProjectDirectory()
                .dir(Directories.getTestbedHome().toFile().getAbsolutePath()));

        getDistributionDir().convention(getDownloadDir().dir(getName()));

        var parts = getUrl().get().split("/");
        var basename = parts[parts.length - 1];

        getBaseImageFile().convention(getDistributionDir().file(basename));
    }
}
