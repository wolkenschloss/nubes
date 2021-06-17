package wolkenschloss;

import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;

class PartialDirectory implements PartialPath<Directory> {

    private final ProjectLayout layout;
    private final String path;

    public PartialDirectory(ProjectLayout layout, String path) {
        this.layout = layout;
        this.path = path;
    }

    public FileCollection source() {
        return layout.getProjectDirectory()
                .dir("src")
                .dir(path)
                .getAsFileTree();
    }

    public Provider<Directory> build() {
        return layout.getBuildDirectory().dir(path);
    }
}
