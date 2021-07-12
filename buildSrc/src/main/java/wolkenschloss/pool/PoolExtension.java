package wolkenschloss.pool;

import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildServiceRegistry;

import java.io.Serializable;

public abstract class PoolExtension implements Serializable {

    public void initialize(BuildServiceRegistry sharedServices) {
        getRootImageName().convention("root.qcow2");
        getCidataImageName().convention("cidata.img");
        getName().convention("testbed");

        getPoolOperations().set(sharedServices.registerIfAbsent(
                "poolops",
                PoolOperations.class,
                spec -> spec.getParameters().getPoolName().set(getName())));
    }

    public abstract Property<String> getName();

    public abstract Property<String> getRootImageName();

    public abstract Property<String> getCidataImageName();

    public abstract Property<PoolOperations> getPoolOperations();
}
