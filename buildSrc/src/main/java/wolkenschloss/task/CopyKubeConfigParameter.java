package wolkenschloss.task;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public interface CopyKubeConfigParameter {

    Property<String> getDomainName();
    DirectoryProperty getRunDirectory();
}
