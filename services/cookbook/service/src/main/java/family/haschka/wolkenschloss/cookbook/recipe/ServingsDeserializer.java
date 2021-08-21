package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

public class ServingsDeserializer implements JsonbDeserializer<Servings> {
    @Override
    public Servings deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        Integer servings = ctx.deserialize(Integer.class, parser);
        return new Servings(servings);
    }
}
