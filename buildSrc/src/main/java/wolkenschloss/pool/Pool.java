package wolkenschloss.pool;

import org.libvirt.StoragePool;
import wolkenschloss.task.CheckedConsumer;

public class Pool {
    private final String name;

    public Pool(@SuppressWarnings("CdiInjectionPointsInspection") String name) {

        this.name = name;
    }


}

