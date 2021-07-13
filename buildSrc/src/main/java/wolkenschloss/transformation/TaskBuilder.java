package wolkenschloss.transformation;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;

import java.util.function.Function;

// Seems to be over designed.
public class TaskBuilder implements Nameable, Groupable, Templatable, Outputable, Registrable, Descriptionable {

    private final TransformationExtension extension;
    private Provider<RegularFile> template;
    private String name;
    private Provider<RegularFile> output;
    private String group;
    private String description;

    private TaskBuilder(TransformationExtension extension) {
        this.extension = extension;
    }

    public static Nameable create(TransformationExtension extension) {
        return new TaskBuilder(extension);
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
    public Outputable template(Function<DirectoryProperty, Provider<RegularFile>> fn) {
        this.template = fn.apply(this.extension.getSourceDirectory());
        return this;
    }

    @Override
    public Registrable outputDescription(Function<DirectoryProperty, Provider<RegularFile>> fn) {
        this.output = fn.apply(this.extension.getGeneratedVirshConfigDirectory());
        return this;
    }

    @Override
    public Registrable outputCloudConfig(Function<DirectoryProperty, Provider<RegularFile>> fn) {
        this.output = fn.apply(this.extension.getGeneratedCloudInitDirectory());
        return this;
    }

    @Override
    public void register(TaskContainer tasks) {
        tasks.register(name, Transform.class, task -> {
            task.setGroup(this.group);
            task.setDescription(this.description);
            task.getTemplate().convention(template);
            task.getOutputFile().convention(output);
        });
    }
}
