package wolkenschloss.gradle.testbed.pool

import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.libvirt.Connect
import org.libvirt.LibvirtException
import org.libvirt.StoragePool
import java.io.File
import java.nio.file.Files
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

abstract class PoolOperations : BuildService<BuildServiceParameters.None>, AutoCloseable {
    private val connection: Connect = Connect("qemu:///system")

    fun destroy(poolId: UUID) {
        fallsPoolExistiert(poolId) { pool ->
            pool.destroy()
            pool.undefine()
        }
    }

    fun create(xmlDescription: Property<File>): UUID {
        val file = xmlDescription.get().absoluteFile.toPath()
        val xml = Files.readString(file)
        val pool = connection.storagePoolDefineXML(xml, 0)
        return try {
            pool.create(0)
            pool.setAutostart(-1)
            UUID.fromString(pool.uuidString)
        } finally {
            pool.free()
        }
    }

    private fun allPools(): List<String> {
        return Stream.concat(
            Arrays.stream(connection.listDefinedStoragePools()),
            Arrays.stream(connection.listStoragePools())
        )
            .collect(Collectors.toList())
    }

    private fun fallsPoolExistiert(poolId: UUID, consumer: (StoragePool) -> Unit) {
        for (poolName in allPools()) {
            val pool = connection.storagePoolLookupByName(poolName)
            try {
                if (pool.uuidString == poolId.toString()) {
                    consumer(pool)
                }
            } finally {
                pool.free()
            }
        }
    }


    fun exists(poolName: String): Boolean {
        return allPools().contains(poolName)
    }

    fun run(poolName: String, consumer: (StoragePool) -> Unit) {
        var pool: StoragePool? = null
        try {
            try {
                pool = connection.storagePoolLookupByName(poolName)
                consumer(pool)
            } catch (exception: LibvirtException) {
                throw RuntimeException("Unknown storage pool $poolName", exception)
            } finally {
                pool?.free()
            }
        } catch (exception: LibvirtException) {
            throw RuntimeException("Can not free storage pool", exception)
        }
    }

    override fun close() {
        connection.close()
    }

    companion object {
        const val POOL_OPERATIONS = "pool-operations"

        fun getInstance(gradle: Gradle): Provider<PoolOperations> {

            return gradle.sharedServices.registerIfAbsent(
                POOL_OPERATIONS,
                PoolOperations::class.java) { }
        }
    }

}