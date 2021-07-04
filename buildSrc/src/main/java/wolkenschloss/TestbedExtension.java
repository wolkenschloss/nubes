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
    abstract public RegularFileProperty getSshKeyFile();

    abstract public Property<String> getName();

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

    abstract public Property<String> getRootImageName();

    abstract public Property<String> getCidataImageName();

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
        property.put("hostname", getView().getHostname());
        property.put("fqdn", getView().getFqdn());
        property.put("locale", getView().getLocale());

        var callback = objects.mapProperty(String.class, Object.class);
        callback.put("ip", getView().getHostAddress());
        callback.put("port", getView().getCallbackPort());
        property.put("callback", callback);

        var disks = objects.mapProperty(String.class, Object.class);
        disks.put("root", getRootImageName());
        disks.put("cidata", getCidataImageName());
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
