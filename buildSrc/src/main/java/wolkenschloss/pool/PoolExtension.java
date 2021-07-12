package wolkenschloss.pool;

import org.gradle.api.provider.Property;

import java.io.Serializable;

public interface PoolExtension extends Serializable {

    Property<String> getName();

    Property<String> getRootImageName();

    Property<String> getCidataImageName();

    Property<PoolOperations> getPoolOperations();
}
