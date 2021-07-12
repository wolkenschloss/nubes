package wolkenschloss;

import org.gradle.api.file.DirectoryProperty;

public interface TransformationExtension {
    DirectoryProperty getSourceDirectory();

    DirectoryProperty getGeneratedCloudInitDirectory();

    DirectoryProperty getGeneratedVirshConfigDirectory();
}
