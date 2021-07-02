package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.libvirt.LibvirtException;

import java.util.List;
import java.util.Map;

public interface BaseTestbedExtension {
    RegularFileProperty getSshKeyFile();

    Property<String> getName();

    @Nested
    TestbedView getView();

    default void view(Action<? super TestbedView> action) {
        action.execute(getView());
    }

    @Nested
    TestbedDomain getDomain();

    default void domain(Action<? super TestbedDomain> action) {action.execute(getDomain());}

    @Nested
    TestbedPool getPool();

    default void pool(Action<? super TestbedPool> action) {action.execute(getPool());}

    Property<String> getRootImageName();

    Property<String> getCidataImageName();

    @Nested
    BaseImage getBaseImage();

    default void base(Action<? super BaseImage> action) {
        action.execute(getBaseImage());
    }

    /**
     * Liefert die Beschreibung des Prüfstandes als Map
     * @param objects Tja
     * @return
     */
    default Provider<Map<String, Object>> asPropertyMap(ObjectFactory objects) {
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

    default Testbed create() throws LibvirtException {
        return new Testbed(getDomain().getName().get());
    }

    DirectoryProperty getPoolDirectory();

    DirectoryProperty getRunDirectory();

    DirectoryProperty getSourceDirectory();

    DirectoryProperty getGeneratedCloudInitDirectory();

    DirectoryProperty getGeneratedVirshConfigDirectory();

}
