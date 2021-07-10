package wolkenschloss.transformation;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

public class TransformationTaskRegistrar {

    private final Project project;

    public TransformationTaskRegistrar(Project project) {
        this.project = project;
    }

    public TaskProvider<Transform> register(String taskName, Provider<RegularFile> template, Provider<RegularFile> output) {
        return project.getTasks().register(taskName, Transform.class, task -> {
            task.getTemplate().convention(template);
            task.getOutputFile().convention(output);
        });
    }
}
