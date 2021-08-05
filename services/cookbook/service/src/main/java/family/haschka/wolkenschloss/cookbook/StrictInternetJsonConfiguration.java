package family.haschka.wolkenschloss.cookbook;

import io.quarkus.jsonb.JsonbConfigCustomizer;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.bind.JsonbConfig;

@Singleton
public class StrictInternetJsonConfiguration implements JsonbConfigCustomizer {

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    Logger logger;

    @Override
    public void customize(JsonbConfig jsonbConfig) {
        logger.info("Enable strict internet JSON");
        jsonbConfig.withStrictIJSON(true);
    }
}
