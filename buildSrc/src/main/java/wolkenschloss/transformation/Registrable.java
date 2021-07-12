package wolkenschloss.transformation;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskProvider;

public interface Registrable {
    TaskProvider<Transform> register(Project project);
}
