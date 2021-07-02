package wolkenschloss;

import org.gradle.api.Action;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;

import java.util.Map;

public interface BaseTestbedExtension {
    RegularFileProperty getSshKeyFile();

    @Nested
    TestbedView getView();

    void view(Action<? super TestbedView> action);

    @Nested
    TestbedDomain getDomain();

    void domain(Action<? super TestbedDomain> action);

    @Nested
    TestbedPool getPool();

    void pool(Action<? super TestbedPool> action);

    Property<String> getRootImageName();

    Property<String> getCidataImageName();

    @Nested
    BaseImage getBaseImage();

    void base(Action<? super BaseImage> action);

    DirectoryProperty getPoolDirectory();

    DirectoryProperty getCloudInitDirectory();

    DirectoryProperty getConfigDirectory();

    /**
     * Liefert die Beschreibung des Prüfstandes als Map
     * @param objects Tja
     * @return
     */
    Provider<Map<String, Object>> asPropertyMap(ObjectFactory objects);
}
