package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.libvirt.LibvirtException;

import java.util.Map;

public abstract class TestbedExtension {

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

        property.put("user", getView().getUser());
        property.put("getSshKey", getView().getSshKey());
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
