package wolkenschloss.task;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Delete;
import org.gradle.api.tasks.Destroys;

abstract public class TestbedCleanTask extends Delete {

    @Destroys
    abstract public DirectoryProperty getBuildDirectory();

    public TestbedCleanTask() {
        getBuildDirectory().convention(super.getProject().getLayout().getBuildDirectory());
        super.getDelete().add(getBuildDirectory());
    }
}
