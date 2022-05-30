package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.job.ImportJobAdapter;
import family.haschka.wolkenschloss.cookbook.recipe.*;
import io.quarkus.jsonb.JsonbConfigCustomizer;
import io.quarkus.mongodb.panache.common.jsonb.ObjectIdDeserializer;
import io.quarkus.mongodb.panache.common.jsonb.ObjectIdSerializer;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@ApplicationScoped
public class Serialization {

    @Inject
    Logger log;

    @Produces
    public Jsonb createJsonb(Instance<JsonbConfigCustomizer> customizers) {

        log.infov("Configure Jsonb");

        // Wird benötigt, um Java Records mit yasson zu serialisieren.
        var visibilityStrategy = new PropertyVisibilityStrategy() {
            @Override
            public boolean isVisible(Field field) {
                return true;
            }

            @Override
            public boolean isVisible(Method method) {
                return false;
            }
        };

        var config = new JsonbConfig()

                .withAdapters(new UnitAdapter(), new ImportJobAdapter())
                .withDeserializers(
                        new ServingsDeserializer(),
                        new RationalDeserializer(),
                        new UriDeserializer(),
                        new ObjectIdDeserializer())
                .withSerializers(
                        new GroupSerializer(),
                        new ServingsSerializer(),
                        new RationalSerializer(),
                        new ObjectIdSerializer())
                .withNullValues(true)
                .withPropertyVisibilityStrategy(visibilityStrategy);

        customizers.forEach(customizer -> customizer.customize(config));

        return JsonbBuilder.create(config);
    }
}
