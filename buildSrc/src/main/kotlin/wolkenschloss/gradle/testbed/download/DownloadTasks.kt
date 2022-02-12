package wolkenschloss.gradle.testbed.download

import org.gradle.api.tasks.TaskContainer

class DownloadTasks(private val extension: BaseImageExtension) {
    fun register(tasks: TaskContainer) {
        tasks.register(DOWNLOAD_DISTRIBUTION_TASK_NAME, DownloadDistribution::class.java) {
            group = GROUP_NAME
            description = "Downloads the base image to a cache for later use."
            baseImageLocation.convention(extension.url)
            distributionName.convention(extension.name)
            baseImage.convention(extension.baseImageFile)
            distributionDir.convention(extension.distributionDir)
            outputs.upToDateWhen { distributionDir.get().asFile.exists() }
        }
    }

    companion object {
        const val DOWNLOAD_DISTRIBUTION_TASK_NAME = "download"
        private const val GROUP_NAME = "download"
    }
}