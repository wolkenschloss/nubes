package family.haschka.wolkenschloss.cookbook;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.net.URI;

public class UriDeserializer implements JsonbDeserializer<URI> {
    @Override
    public URI deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        var input = ctx.deserialize(String.class, parser);
        return URI.create(input);
    }
}

