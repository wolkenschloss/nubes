package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;
import java.util.Arrays;


public class GroupSerializer implements JsonbSerializer<Group> {

    @Override
    public void serialize(Group group, JsonGenerator generator, SerializationContext context) {
            generator.writeStartObject();
            generator.write("name", group.name);
            generator.writeStartArray("units");
            Arrays.asList(group.units).forEach(unit -> context.serialize(unit, generator));
            generator.writeEnd();
            generator.writeEnd();
    }
}
