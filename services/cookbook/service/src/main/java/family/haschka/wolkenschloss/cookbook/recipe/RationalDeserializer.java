package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;

public class RationalDeserializer implements JsonbDeserializer<Rational> {

    @Override
    public Rational deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        var rational = ctx.deserialize(String.class, parser);
        return Rational.Companion.parse(rational);
    }
}
