package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.adapter.JsonbAdapter;

public class IngredientAdapter implements JsonbAdapter<Ingredient, IngredientAnnotated> {
    @Override
    public IngredientAnnotated adaptToJson(Ingredient obj) {
        return new IngredientAnnotated(obj.quantity(), obj.unit(), obj.name());
    }

    @Override
    public Ingredient adaptFromJson(IngredientAnnotated obj) {
//        Objects.requireNonNull(obj.name);
        return new Ingredient(obj.quantity, obj.unit, obj.name);
    }
}
