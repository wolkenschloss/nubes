package family.haschka.wolkenschloss.cookbook.logging;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Collections;
import java.util.Map;

public class LogLevelError implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        return Collections.singletonMap("quarkus.log.level", "ERROR");
    }

    @Override
    public void stop() {
    }
}
