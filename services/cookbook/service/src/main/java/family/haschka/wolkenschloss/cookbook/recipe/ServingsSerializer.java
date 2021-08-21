package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class ServingsSerializer implements JsonbSerializer<Servings> {
    @Override
    public void serialize(Servings obj, JsonGenerator generator, SerializationContext ctx) {
        ctx.serialize(obj.amount(), generator);
    }
}
