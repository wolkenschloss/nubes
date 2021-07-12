package wolkenschloss;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.ProjectLayout;

public abstract class TransformationExtension {

    public void initialize(ProjectLayout layout) {
        getGeneratedCloudInitDirectory().set(layout.getBuildDirectory().dir("cloud-init"));
        getGeneratedVirshConfigDirectory().set(layout.getBuildDirectory().dir("config"));
        getSourceDirectory().set(layout.getProjectDirectory().dir("src"));
    }

    public abstract DirectoryProperty getSourceDirectory();

    public abstract DirectoryProperty getGeneratedCloudInitDirectory();

    public abstract DirectoryProperty getGeneratedVirshConfigDirectory();
}
