package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;
import org.jboss.logging.Logger;
import org.jetbrains.annotations.Nullable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeAdapter implements JsonbAdapter<Recipe, RecipeAnnotations> {
    @Override
    public RecipeAnnotations adaptToJson(Recipe obj) {

        var ingredients = Json.createArrayBuilder();
        obj.ingredients().forEach(i -> {
            var ingredient = Json.createObjectBuilder();
            Optional.ofNullable(i.getQuantity()).ifPresent(quantity -> ingredient.add("quantity", quantity.toString()));
            Optional.ofNullable(i.getUnit()).ifPresent(unit -> ingredient.add("unit", unit));
            ingredient.add("name", i.getName());
            ingredients.add(ingredient.build());
        });

        var json = new RecipeAnnotations();
        json._id = Optional.ofNullable(obj._id()).map(ObjectId::new).orElse(null);
        json.title = obj.title();
        json.preparation = obj.preparation();
        json.ingredients = ingredients.build();
        json.servings = new Servings(obj.servings().getAmount());
        json.created = obj.created();
        return json;
    }

    @Override
    public Recipe adaptFromJson(RecipeAnnotations obj) {
        try {


            var x = Optional.ofNullable(obj.ingredients)
                    .map(ingredients -> ingredients.stream()
                            .map(JsonValue::asJsonObject)
                            .map(o -> new Ingredient(
                                    getQuantity(o),
                                    o.containsKey("unit") ? o.getString("unit") : null,
                                    o.getString("name"))).collect(Collectors.toList()))
                    .orElse(new ArrayList<>());

            return new Recipe(
                    Optional.ofNullable(obj._id).map(ObjectId::toHexString).orElse(null),
                    obj.title,
                    obj.preparation,
                    x,
                    Optional.ofNullable(obj.servings).map(servings -> new Servings(servings.getAmount())).orElse(new Servings(1)),
                    0L);
        } catch (Exception exception) {
            Logger.getLogger(RecipeAdapter.class).error("Kann das nicht deserialisieren.", exception);
            throw exception;
        }
    }

    @Nullable
    private Rational getQuantity(JsonObject o) {
        if (o.containsKey("quantity")) {
            var typ = o.get("quantity").getValueType();
            if (typ == JsonValue.ValueType.NUMBER) {
                var num = o.getJsonNumber("quantity");
                return new Rational(num.intValue());
            } else if (typ == JsonValue.ValueType.STRING) {
                return Rational.parse(o.getString("quantity"));
            }
        }

        return null;
    }
}
