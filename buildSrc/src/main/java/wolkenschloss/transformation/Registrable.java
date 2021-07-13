package wolkenschloss.transformation;

import org.gradle.api.tasks.TaskContainer;

public interface Registrable {
    void register(TaskContainer tasks);
}
