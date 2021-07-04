package wolkenschloss;

import org.gradle.api.provider.Property;

import java.io.Serializable;

public interface PoolExtension extends Serializable {

    Property<String> getName();
//    Property<String> getPath();

    // Pool
    abstract public Property<String> getRootImageName();
}
