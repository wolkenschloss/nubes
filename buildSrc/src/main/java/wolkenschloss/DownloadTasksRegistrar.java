package wolkenschloss;

import org.gradle.api.tasks.TaskContainer;
import wolkenschloss.download.BaseImageExtension;
import wolkenschloss.download.DownloadDistribution;

public class DownloadTasksRegistrar {
    public static final String DOWNLOAD_DISTRIBUTION_TASK_NAME = "download";

    private final TaskContainer tasks;

    public DownloadTasksRegistrar(TaskContainer tasks) {
        this.tasks = tasks;
    }

    public void register(BaseImageExtension baseImage) {
        getTasks().register(
                DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class,
                task -> {
                    task.getBaseImageLocation().convention(baseImage.getUrl());
                    task.getDistributionName().convention(baseImage.getName());
                    task.getBaseImage().convention(baseImage.getBaseImageFile());
                    task.getDistributionDir().convention(baseImage.getDistributionDir());
                });
    }

    public TaskContainer getTasks() {
        return tasks;
    }

}
