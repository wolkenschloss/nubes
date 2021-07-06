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

    @Nonnull
    public static String readSshKey(RegularFile sshKeyFile) {

        var file = sshKeyFile.getAsFile();

        try {
            return Files.readString(file.toPath()).trim();
        } catch (IOException e) {
            throw new GradleScriptException("Can not read public ssh key", e);
        }
    }

    public void configure(ProjectLayout layout) {
        // Set build directories
        var buildDirectory = layout.getBuildDirectory();
        getPoolDirectory().set(buildDirectory.dir("pool"));
        getRunDirectory().set(buildDirectory.dir("run"));
        getGeneratedCloudInitDirectory().set(buildDirectory.dir("cloud-init"));
        getGeneratedVirshConfigDirectory().set(buildDirectory.dir("config"));

        getSourceDirectory().set(layout.getProjectDirectory().dir("src"));

        getUser().getSshKeyFile().convention(() -> Path.of(System.getenv("HOME"), ".ssh", "id_rsa.pub").toFile());
        getUser().getSshKey().convention(getUser().getSshKeyFile().map(TestbedExtension::readSshKey));
        getUser().getName().convention(System.getenv("USER"));

        getDomain().getName().convention("testbed");
        getDomain().getFqdn().convention("testbed.wolkenschloss.local");
        getDomain().getLocale().convention(System.getenv("LANG"));

        getHost().getHostAddress().convention(IpUtil.getHostAddress());
        getHost().getCallbackPort().set(DEFAULT_CALLBACK_PORT);

        getPool().getRootImageName().convention("root.qcow2");
        getPool().getCidataImageName().convention("cidata.img");
        getPool().getName().convention("testbed");

        getBaseImage().initialize();
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

    @Nested
    abstract public DomainExtension getDomain();

    public void domain(Action<? super DomainExtension> action) {action.execute(getDomain());}

    @Nested
    abstract public PoolExtension getPool();

    public void pool(Action<? super PoolExtension> action) {action.execute(getPool());}

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
        property.put("sshKey", getUser().getSshKey());
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

    abstract public DirectoryProperty getPoolDirectory();

    abstract public DirectoryProperty getRunDirectory();

    abstract public DirectoryProperty getSourceDirectory();

    abstract public DirectoryProperty getGeneratedCloudInitDirectory();

    abstract public DirectoryProperty getGeneratedVirshConfigDirectory();
}
