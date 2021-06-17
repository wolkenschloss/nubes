package wolkenschloss;

import org.gradle.api.file.ProjectLayout;

class PartialPathBuilder {

    private ProjectLayout layout;

    public PartialPathBuilder(ProjectLayout layout) {

        this.layout = layout;
    }

    public PartialDirectory dir(String s) {
        return new PartialDirectory(layout, s);
    }

    public PartialFile file(String path) {
        return new PartialFile(layout, path);
    }
}
