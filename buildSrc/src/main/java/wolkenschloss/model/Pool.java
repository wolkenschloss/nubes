package wolkenschloss.model;

import org.libvirt.StoragePool;
import wolkenschloss.task.CheckedConsumer;

public class Pool {
    private final String name;

    public Pool(@SuppressWarnings("CdiInjectionPointsInspection") String name) {

        this.name = name;
    }

    public void run(CheckedConsumer<StoragePool> consumer) throws Throwable {
        var connection = new org.libvirt.Connect("qemu:///system");
        var pool = connection.storagePoolLookupByName(name);
        consumer.accept(pool);
        pool.free();
        connection.close();
    }
}

