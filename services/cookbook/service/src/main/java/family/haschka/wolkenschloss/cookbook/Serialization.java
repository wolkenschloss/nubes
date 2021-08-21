package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.recipe.ServingsDeserializer;
import family.haschka.wolkenschloss.cookbook.recipe.ServingsSerializer;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

@ApplicationScoped
public class Serialization {

    @Produces
    public Jsonb createJsonb() {
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
                .withDeserializers(new ServingsDeserializer())
                .withSerializers(new ServingsSerializer())
                .withPropertyVisibilityStrategy(visibilityStrategy);
        return JsonbBuilder.create(config);
    }
}
