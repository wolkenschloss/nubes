package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.Arrays;

public class UnitAdapter implements JsonbAdapter<Unit, JsonObject> {
    @Override
    public JsonObject adaptToJson(Unit unit) {
        return Json.createObjectBuilder()
                .add("name", unit.name())
                .add("values", Json.createArrayBuilder(Arrays.asList(unit.aliases)).add(unit.unit))
                .build();
    }

    @Override
    public Unit adaptFromJson(JsonObject jsonObject) {
        return null;
    }
}
