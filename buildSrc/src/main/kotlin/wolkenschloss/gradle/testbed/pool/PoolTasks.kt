package wolkenschloss.gradle.testbed.pool

import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.existing
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import wolkenschloss.gradle.testbed.download.DownloadDistribution
import wolkenschloss.gradle.testbed.transformation.networkConfig
import wolkenschloss.gradle.testbed.transformation.poolDescription
import wolkenschloss.gradle.testbed.transformation.userData

class PoolTasks(private val extension: PoolExtension) {
    fun register(tasks: TaskContainer) {
        registerBuildDataSourceImageTask(tasks)
        registerBuildRootImageTask(tasks)
        registerBuildPoolTask(tasks)
    }

    private fun registerBuildPoolTask(tasks: TaskContainer) {
        val buildDataSourceImage = tasks.findByName(BUILD_DATA_SOURCE_IMAGE_TASK_NAME)
        val buildRootImage = tasks.findByName(BUILD_ROOT_IMAGE_TASK_NAME)

        val transform by tasks.existing(Copy::class)

        tasks.register(BUILD_POOL_TASK_NAME, BuildPool::class.java) {
            group = GROUP_NAME
            description =
                "Defines a virtlib storage pool based on the transformed description file containing a root image and a cloud-init data source volume."
            poolOperations.set(extension.poolOperations)
            this.poolDescriptionFile.convention(transform.poolDescription)
            poolRunFile.convention(extension.poolRunFile)
            dependsOn(buildRootImage, buildDataSourceImage)
        }
    }

    private fun registerBuildRootImageTask(tasks: TaskContainer) {
        val download by tasks.existing(DownloadDistribution::class)

        tasks.register(BUILD_ROOT_IMAGE_TASK_NAME, BuildRootImage::class.java) {
            group = GROUP_NAME
            description = "Creates the root image for the later domain from a downloaded base image."
            size.convention(DEFAULT_IMAGE_SIZE)
            baseImage.convention(download.flatMap { it.baseImage })
            rootImage.convention(extension.poolDirectory.file(extension.rootImageName))
            rootImageMd5File.convention(extension.rootImageMd5File)
        }
    }

    private fun registerBuildDataSourceImageTask(tasks: TaskContainer) {
        val transform by tasks.existing(Copy::class)

        val dataSourceImage1: Provider<RegularFile> = extension.poolDirectory.file(extension.cidataImageName)

        tasks.register(BUILD_DATA_SOURCE_IMAGE_TASK_NAME, BuildDataSourceImage::class.java) {
            group = GROUP_NAME
            description =
                "Generates a cloud-init data source volume containing the transformed network-config and user-data files."
            networkConfig.convention(transform.networkConfig)
            userData.convention(transform.userData)
            dataSourceImage.convention(dataSourceImage1)
        }
    }

    companion object {
        const val BUILD_POOL_TASK_NAME = "buildPool"
        const val BUILD_ROOT_IMAGE_TASK_NAME = "buildRootImage"
        const val BUILD_DATA_SOURCE_IMAGE_TASK_NAME = "buildDataSourceImage"
        private const val DEFAULT_IMAGE_SIZE = "20G"
        private const val GROUP_NAME = "pool"
    }
}

