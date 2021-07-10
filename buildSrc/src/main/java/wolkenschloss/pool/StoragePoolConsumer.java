package wolkenschloss.pool;

import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;

@FunctionalInterface
public interface StoragePoolConsumer {
    void accept(StoragePool pool) throws LibvirtException;
}
