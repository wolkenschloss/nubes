package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.libvirt.LibvirtException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public abstract class TestbedExtension {

    public static final int DEFAULT_CALLBACK_PORT = 9191;

    public static void configure(TestbedExtension extension, ProjectLayout layout) {
        // Set build directories
        var buildDirectory = layout.getBuildDirectory();
        extension.getPoolDirectory().set(buildDirectory.dir("pool"));
        extension.getRunDirectory().set(buildDirectory.dir("run"));
        extension.getGeneratedCloudInitDirectory().set(buildDirectory.dir("cloud-init"));
        extension.getGeneratedVirshConfigDirectory().set(buildDirectory.dir("config"));

        extension.getSourceDirectory().set(layout.getProjectDirectory().dir("src"));

        extension.getUser().getSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile());
        extension.getUser().getSshKey().convention(extension.getUser().getSshKeyFile().map(TestbedExtension::readSshKey));
        extension.getUser().getName().convention(System.getenv("USER"));

        extension.getDomain().getName().convention("testbed");
        extension.getDomain().getFqdn().convention("testbed.wolkenschloss.local");
        extension.getDomain().getLocale().convention(System.getenv("LANG"));

        extension.getHost().getHostAddress().convention(IpUtil.getHostAddress());
        extension.getHost().getCallbackPort().set(DEFAULT_CALLBACK_PORT);

        extension.getPool().getRootImageName().convention("root.qcow2");
        extension.getPool().getCidataImageName().convention("cidata.img");
        extension.getPool().getName().convention("testbed");

        extension.getBaseImage().getUrl().convention("https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img");
        extension.getBaseImage().getName().convention("ubuntu-20.04");
    }

    @Nonnull
    public static String readSshKey(RegularFile sshKeyFile) {

        var file = sshKeyFile.getAsFile();

        try {
            return Files.readString(file.toPath()).trim();
        } catch (IOException e) {
            throw new GradleScriptException("Can not read public ssh key", e);
        }
    }

    @Nested
    abstract public HostExtension getHost();
    public void host(Action<? super HostExtension> action) {
        action.execute(getHost());
    }

    @Nested
    abstract public UserExtension getUser();
    public void user(Action<? super UserExtension> action) {
        action.execute(getUser());
    }

    // ???
    @Nested
    abstract public TestbedView getView();

    public void view(Action<? super TestbedView> action) {
        action.execute(getView());
    }

    @Nested
    abstract public DomainExtension getDomain();

    public void domain(Action<? super DomainExtension> action) {action.execute(getDomain());}

    @Nested
    abstract public PoolExtension getPool();

    public void pool(Action<? super PoolExtension> action) {action.execute(getPool());}

    // Pool
    @Nested
    abstract public BaseImageExtension getBaseImage();

    public void base(Action<? super BaseImageExtension> action) {
        action.execute(getBaseImage());
    }

    /**
     * Liefert die Beschreibung des Prüfstandes als Map
     * @param objects Tja
     * @return
     */
    public Provider<Map<String, Object>> asPropertyMap(ObjectFactory objects) {
        var property = objects.mapProperty(String.class, Object.class);

        property.put("user", getUser().getName());
        property.put("getSshKey", getUser().getSshKey());
        property.put("hostname", getDomain().getName());
        property.put("fqdn", getDomain().getFqdn());
        property.put("locale", getDomain().getLocale());

        var callback = objects.mapProperty(String.class, Object.class);
        callback.put("ip", getHost().getHostAddress());
        callback.put("port", getHost().getCallbackPort());
        property.put("callback", callback);

        var disks = objects.mapProperty(String.class, Object.class);
        disks.put("root", getPool().getRootImageName());
        disks.put("cidata", getPool().getCidataImageName());
        property.put("disks", disks);

        var pool = objects.mapProperty(String.class, Object.class);
        pool.put("name", getPool().getName());
        pool.put("directory", getPoolDirectory());
        property.put("pool", pool);

        return property;
    }

    public Testbed create() throws LibvirtException {
        return new Testbed(getDomain().getName().get());
    }

    // Host? Pool?
    abstract public DirectoryProperty getPoolDirectory();

    // Host?
    abstract public DirectoryProperty getRunDirectory();

    abstract public DirectoryProperty getSourceDirectory();

    // Pool?
    abstract public DirectoryProperty getGeneratedCloudInitDirectory();

    // Domain? Pool?
    abstract public DirectoryProperty getGeneratedVirshConfigDirectory();
}
