package wolkenschloss;


import org.gradle.api.provider.Property;

import java.io.Serializable;
import java.util.HashMap;

public interface TestbedPool extends Serializable {
    default HashMap<String, Object> toTemplateScope() {
        var pool = new HashMap<String, Object>();

        var poolName = getName().get();
        var poolDir = getPath().get();
        pool.put("name", poolName);
        pool.put("directory", poolDir);
        return pool;
    }

    Property<String> getName();
    Property<String> getPath();
}
