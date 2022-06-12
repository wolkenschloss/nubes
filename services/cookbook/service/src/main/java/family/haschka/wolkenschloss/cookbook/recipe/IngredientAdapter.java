package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.adapter.JsonbAdapter;

public class IngredientAdapter implements JsonbAdapter<Ingredient, IngredientAnnotations> {
    @Override
    public IngredientAnnotations adaptToJson(Ingredient obj) {
        var result = new IngredientAnnotations();
        result.name = obj.getName();
        result.unit = obj.getUnit();
        result.quantity = obj.getQuantity();

        return result;
    }

    @Override
    public Ingredient adaptFromJson(IngredientAnnotations obj) {
        return new Ingredient(obj.name, obj.quantity,obj.unit);
    }
}
