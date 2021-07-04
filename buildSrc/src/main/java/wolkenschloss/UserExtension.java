package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public interface UserExtension {
    RegularFileProperty getSshKeyFile();

    Property<String> getSshKey();

    Property<String> getUser();
}
