package familie.haschka.wolkenschloss.cookbook.testing;

import io.quarkus.test.common.DevServicesContext;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class MongoDbHelperResource implements QuarkusTestResourceLifecycleManager, DevServicesContext.ContextAware {
    @Override
    public Map<String, String> start() {
        return null;
    }

    @Override
    public void stop() {

    }

    @Override
    public void setIntegrationTestContext(DevServicesContext context) {

    }
}
