package wolkenschloss.gradle.testbed.pool

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceRegistry
import java.io.Serializable

abstract class PoolExtension : Serializable {
    fun initialize(
        sharedServices: BuildServiceRegistry,
        buildDirectory: DirectoryProperty,
        runDirectory: DirectoryProperty
    ) {
        rootImageName.convention("root.qcow2")
        cidataImageName.convention("cidata.img")
        name.convention("testbed")
        poolOperations.set(
            sharedServices.registerIfAbsent(
                POOL_OPERATIONS,
                PoolOperations::class.java) { parameters.poolName.set(name) })
        poolDirectory.set(buildDirectory.dir("pool"))
        rootImageMd5File.set(runDirectory.file(DEFAULT_RUN_FILE_NAME))
        poolRunFile.set(runDirectory.file(DEFAULT_POOL_RUN_FILE))
    }

    abstract val name: Property<String?>
    abstract val rootImageName: Property<String?>
    abstract val cidataImageName: Property<String?>
    abstract val poolOperations: Property<PoolOperations?>
    abstract val poolDirectory: DirectoryProperty
    abstract val rootImageMd5File: RegularFileProperty
    abstract val poolRunFile: RegularFileProperty

    companion object {
        const val DEFAULT_RUN_FILE_NAME = "root.md5"
        const val DEFAULT_POOL_RUN_FILE = "pool.run"
        const val POOL_OPERATIONS = "pool-operations"
    }
}