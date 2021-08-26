package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServingsDeserializer implements JsonbDeserializer<Servings> {
    @Override
    public Servings deserialize(JsonParser parser, DeserializationContext ctx, Type rtType) {
        var input = ctx.deserialize(String.class, parser);
        Pattern p = Pattern.compile("(?<servings>[1-9][0-9]*)");
        Matcher m = p.matcher(input);
        if (m.find()) {
            String servings = m.group("servings");
            return new Servings(Integer.parseInt(servings));
        } else
            return new Servings(1);
    }
}
