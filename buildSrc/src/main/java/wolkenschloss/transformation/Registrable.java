package wolkenschloss.transformation;

import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

public interface Registrable {
    void register(TaskContainer tasks);
}
