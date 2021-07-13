package wolkenschloss.transformation;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskContainer;

public class TransformationTasks {
    public static final String NETWORK_CONFIG_FILE_NAME = "network-config";
    public static final String USER_DATA_FILE_NAME = "user-data";
    public static final String POOL_DESCRIPTION_FILE_NAME = "pool.xml";
    public static final String DOMAIN_DESCRIPTION_FILE_NAME = "domain.xml";

    public static final String TEMPLATE_FILENAME_EXTENSION = "mustache";

    public static final String TRANSFORM_NETWORK_CONFIG_TASK_NAME = "transformNetworkConfig";
    public static final String TRANSFORM_USER_DATA_TASK_NAME = "transformUserData";
    public static final String TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME = "transformDomainDescription";
    public static final String TRANSFORM_POOL_DESCRIPTION_TASK_NAME = "transformPoolDescription";


    private final Project project;
    private final String group;
    private final TaskContainer tasks;

    public TransformationTasks(TaskContainer tasks, Project project, String group) {
        this.project = project;
        this.group = group;
        this.tasks = tasks;
    }

    public void registerTransformPoolDescriptionTask(TransformationExtension extension) {
        TaskBuilder.create(extension)
                .name(TRANSFORM_POOL_DESCRIPTION_TASK_NAME)
                .group(group)
                .description("Transforms pool.xml template")
                .template(src -> src.file(templateFilename(POOL_DESCRIPTION_FILE_NAME)))
                .outputDescription(dst -> dst.file(POOL_DESCRIPTION_FILE_NAME))
                .register(tasks);
    }

    public void registerTransformDomainDescriptionTask(TransformationExtension extension) {
        TaskBuilder.create(extension)
                .name(TRANSFORM_DOMAIN_DESCRIPTION_TASK_NAME)
                .group(group)
                .description("Transforms domain.xml")
                .template(src -> src.file(templateFilename(DOMAIN_DESCRIPTION_FILE_NAME)))
                .outputDescription(dst -> dst.file(DOMAIN_DESCRIPTION_FILE_NAME))
                .register(tasks);
    }

    public void registerTransformUserDataTask(TransformationExtension extension) {
        TaskBuilder.create(extension)
                .name(TRANSFORM_USER_DATA_TASK_NAME)
                .group(group)
                .description("Transforms user-data template")
                .template(src -> src.file(templateFilename(USER_DATA_FILE_NAME)))
                .outputCloudConfig(dst -> dst.file(USER_DATA_FILE_NAME))
                .register(tasks);
    }

    public void registerTransformNetworkConfigTask(TransformationExtension extension) {
        TaskBuilder.create(extension)
                .name(TRANSFORM_NETWORK_CONFIG_TASK_NAME)
                .group(group)
                .description("Transforms network-config template")
                .template(src -> src.file(templateFilename(NETWORK_CONFIG_FILE_NAME)))
                .outputCloudConfig(dst -> dst.file(NETWORK_CONFIG_FILE_NAME))
                .register(tasks);
    }

    public void register(TransformationExtension extension) {
        registerTransformUserDataTask(extension);
        registerTransformNetworkConfigTask(extension);
        registerTransformPoolDescriptionTask(extension);
        registerTransformDomainDescriptionTask(extension);
    }

    private static String templateFilename(String filename) {
        return String.format("%s.%s", filename, TEMPLATE_FILENAME_EXTENSION);
    }
}
