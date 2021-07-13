package wolkenschloss.download;

import org.gradle.api.tasks.TaskContainer;

public class DownloadTasksRegistrar {
    public static final String DOWNLOAD_DISTRIBUTION_TASK_NAME = "download";
    public static final String GROUP_NAME = "download";

    private final TaskContainer tasks;

    public DownloadTasksRegistrar(TaskContainer tasks) {
        this.tasks = tasks;
    }

    public void register(BaseImageExtension baseImage) {
        getTasks().register(
                DOWNLOAD_DISTRIBUTION_TASK_NAME,
                DownloadDistribution.class,
                task -> {
                    task.setGroup(GROUP_NAME);
                    task.setDescription("Downloads the base image to a cache for later use.");
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
