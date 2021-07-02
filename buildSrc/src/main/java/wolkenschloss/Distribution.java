package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import java.nio.file.Path;

public class Distribution {

    private ObjectFactory objects;
    private final Property<String> name;

    public Distribution(ObjectFactory objects, @SuppressWarnings("CdiInjectionPointsInspection") Property<String> name) {
        this.objects = objects;

        this.name = name;
    }

    public Path getXdgDataHome() {
        var dataDir = System.getenv().get("XDG_DATA_HOME");
        if (dataDir == null) {
            return Path.of(System.getenv("HOME"), ".local", "share");
        } else {
            return Path.of(dataDir);
        }
    }

    public Path getDistributionDir() {
        return getDownloadDir().resolve(this.name.get());
    }

    public Path getDownloadDir() {
        var data = getXdgDataHome();
        return data.resolve("testbed");
    }

    public Property<String> getName() {
        return name;
    }

    public RegularFileProperty file(String basename) {
        var property = objects.fileProperty();
        property.set (getDistributionDir().resolve(basename).toFile());
        return property;
    }
}
