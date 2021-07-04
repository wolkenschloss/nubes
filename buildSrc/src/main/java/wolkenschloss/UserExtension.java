package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;

public interface UserExtension {
    RegularFileProperty getSshKeyFile();
}
