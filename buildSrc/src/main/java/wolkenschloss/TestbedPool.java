package wolkenschloss;

import org.gradle.api.provider.Property;

import java.io.Serializable;

public interface TestbedPool extends Serializable {

    Property<String> getName();
    Property<String> getPath();
}
