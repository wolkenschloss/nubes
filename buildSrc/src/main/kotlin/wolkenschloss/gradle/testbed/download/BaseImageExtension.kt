package wolkenschloss.gradle.testbed.download

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
// TODO: In der Hierarchy nicht nach oben greifen
import wolkenschloss.gradle.testbed.Directories
import javax.inject.Inject

abstract class BaseImageExtension {
    @get:Inject
    abstract val objects: ObjectFactory?
    abstract val url: Property<String>
    abstract val name: Property<String?>
    abstract val downloadDir: DirectoryProperty
    abstract val distributionDir: DirectoryProperty
    abstract val baseImageFile: RegularFileProperty
    fun initialize(project: Project) {
        url.convention(DEFAULT_DOWNLOAD_URL)
        name.convention(DEFAULT_DISTRIBUTION_NAME)
        downloadDir.set(
            project.layout
                .projectDirectory
                .dir(Directories.testbedHome.toFile().absolutePath)
        )
        distributionDir.convention(downloadDir.dir(name))
        val parts = url.get().split("/".toRegex()).toTypedArray()
        val basename = parts[parts.size - 1]
        baseImageFile.convention(distributionDir.file(basename))
    }

    companion object {
        const val DEFAULT_DOWNLOAD_URL =
            "https://cloud-images.ubuntu.com/focal/current/focal-server-cloudimg-amd64-disk-kvm.img"
        const val DEFAULT_DISTRIBUTION_NAME = "ubuntu-20.04"
        const val APP_NAME = "testbed"
    }
}