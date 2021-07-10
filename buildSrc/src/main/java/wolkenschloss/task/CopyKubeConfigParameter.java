package wolkenschloss.task;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public interface CopyKubeConfigParameter {

    Property<String> getDomainName();
    RegularFileProperty getKubeConfigFile();
}
