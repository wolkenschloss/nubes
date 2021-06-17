package wolkenschloss;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

public class PartialFile implements PartialPath<RegularFile> {
    private ProjectLayout layout;
    private String p1;

    public PartialFile(ProjectLayout layout, String p1) {

        this.layout = layout;
        this.p1 = p1;
    }

    @Override
    public FileCollection source() {
        return layout.getProjectDirectory().dir("src").files(p1);
    }

    @Override
    public Provider<RegularFile> build() {
        return layout.getBuildDirectory().file(p1);
    }
}
