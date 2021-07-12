package wolkenschloss.transformation;

import org.gradle.api.Project;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;

// Seems to be over designed.
public class TaskRegistrar implements Nameable, Groupable, Templatable, Outputable, Registrable, Descriptionable {

    private Provider<RegularFile> template;
    private String name;
    private Provider<RegularFile> output;
    private String group;
    private String description;

    private TaskRegistrar() {}

    public static Nameable create() {
        return new TaskRegistrar();
    }

    @Override
    public Groupable name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public Descriptionable group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public Templatable description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public Outputable template(Provider<RegularFile> template) {
        this.template = template;
        return this;
    }

    @Override
    public Registrable output(Provider<RegularFile> output) {
        this.output = output;
        return this;
    }

    @Override
    public TaskProvider<Transform> register(Project project) {
        return project.getTasks().register(name, Transform.class, task -> {
            task.setGroup(this.group);
            task.setDescription(this.description);
            task.getTemplate().convention(template);
            task.getOutputFile().convention(output);
        });
    }
}
