package wolkenschloss.task.status;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

public interface StatusTaskParameter {
    @Internal
    Property<String> getDomainName();

    @Internal
    Property<String> getPoolName();

    @Internal
    Property<String> getDistributionName();

    @Internal
    DirectoryProperty getDownloadDir();

    @Internal
    DirectoryProperty getDistributionDir();

    @Internal
    RegularFileProperty getBaseImageFile();
}
