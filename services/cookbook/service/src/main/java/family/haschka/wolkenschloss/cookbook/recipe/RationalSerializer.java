package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

public class RationalSerializer implements JsonbSerializer<Rational> {
    @Override
    public void serialize(Rational obj, JsonGenerator generator, SerializationContext ctx) {
        ctx.serialize(obj.toString(), generator);
    }
}
