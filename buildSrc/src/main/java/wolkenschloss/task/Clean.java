package wolkenschloss.task;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.Destroys;

abstract public class Clean extends Delete {

    @Destroys
    abstract public DirectoryProperty getBuildDirectory();

    public Clean() {
        getBuildDirectory().convention(super.getProject().getLayout().getBuildDirectory());
        super.getDelete().add(getBuildDirectory());
    }
}
