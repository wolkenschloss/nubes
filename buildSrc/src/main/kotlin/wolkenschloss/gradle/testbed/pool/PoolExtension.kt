package wolkenschloss.gradle.testbed.pool

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import java.io.Serializable
import javax.inject.Inject

@Suppress("CdiInjectionPointsInspection")
abstract class PoolExtension @Inject constructor(private val layout: ProjectLayout) : Serializable {
    fun initialize(runDirectory: DirectoryProperty) {
        rootImageName.convention(DEFAULT_ROOT_IMAGE_NAME)
        cidataImageName.convention(DEFAULT_CIDATA_IMAGE_NAME)
        name.convention("testbed")
        poolDirectory.set(layout.buildDirectory.dir("pool"))
        rootImageMd5File.set(runDirectory.file(DEFAULT_RUN_FILE_NAME))
        poolRunFile.set(runDirectory.file(DEFAULT_POOL_RUN_FILE))
    }

    abstract val name: Property<String>
    abstract val rootImageName: Property<String>
    abstract val cidataImageName: Property<String>
    abstract val poolDirectory: DirectoryProperty
    abstract val rootImageMd5File: RegularFileProperty
    abstract val poolRunFile: RegularFileProperty

    companion object {
        const val DEFAULT_CIDATA_IMAGE_NAME = "cidata.img"
        const val DEFAULT_ROOT_IMAGE_NAME = "root.qcow2"
        const val DEFAULT_RUN_FILE_NAME = "root.md5"
        const val DEFAULT_POOL_RUN_FILE = "pool.run"

    }
}