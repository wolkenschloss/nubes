package wolkenschloss.gradle.testbed

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Destroys
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.libvirt.LibvirtException
import wolkenschloss.gradle.testbed.domain.DomainOperations
import wolkenschloss.gradle.testbed.pool.PoolOperations
import java.util.*
import javax.inject.Inject

abstract class Destroy : DefaultTask() {
    @get:Destroys
    abstract val poolRunFile: RegularFileProperty

    @get:Destroys
    abstract val buildDir: DirectoryProperty

    @get:Internal
    abstract val domain: Property<String>

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @get:Internal
    abstract val poolOperations: Property<PoolOperations>

    @get:Internal
    abstract val domainOperations: Property<DomainOperations>

    @get:Inject
    abstract val providerFactory: ProviderFactory
    @TaskAction
    @Throws(LibvirtException::class)
    fun destroy() {
        destroyDomain()
        destroyPool()
        deleteBuildDirectory()
    }

    @Throws(LibvirtException::class)
    private fun destroyDomain() {
        val domainOperations: DomainOperations = domainOperations.get()
        val deleted = domainOperations.deleteDomainIfExists(domain)
        if (deleted) {
            logger.info("Domain deleted.")
        }
    }

    @Throws(LibvirtException::class)
    private fun destroyPool() {
        if (poolRunFile.get().asFile.exists()) {
            val content = providerFactory.fileContents(poolRunFile)
            val uuid = content.asText.map { name: String? -> UUID.fromString(name) }.get()
            poolOperations.get().destroy(uuid)
            fileSystemOperations.delete { delete(poolRunFile) }
        }
    }

    private fun deleteBuildDirectory() {
        fileSystemOperations.delete { delete(buildDir) }
    }
}
