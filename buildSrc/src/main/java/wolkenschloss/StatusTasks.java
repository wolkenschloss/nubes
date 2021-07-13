package wolkenschloss;

import wolkenschloss.domain.DomainExtension;
import wolkenschloss.pool.PoolExtension;

public class StatusTasks {
    public final DomainExtension domain;
    public final PoolExtension pool;

    public StatusTasks(DomainExtension domain, PoolExtension pool) {
        this.domain = domain;
        this.pool = pool;
    }
}
