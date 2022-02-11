package wolkenschloss.gradle.testbed.pool

import org.libvirt.LibvirtException
import org.libvirt.StoragePool

fun interface StoragePoolConsumer {
    @Throws(LibvirtException::class)
    fun accept(pool: StoragePool)
}