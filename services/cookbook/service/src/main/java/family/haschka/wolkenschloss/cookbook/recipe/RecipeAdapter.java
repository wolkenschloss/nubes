package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.Optional;

public class RecipeAdapter implements JsonbAdapter<Recipe, RecipeAnnotations> {
    @Override
    public RecipeAnnotations adaptToJson(Recipe obj) {
        var json = new RecipeAnnotations();
        json._id = obj._id();
        json.title = obj.title();
        json.preparation = obj.preparation();
        json.ingredients = new ArrayList<>(obj.ingredients());
        json.servings = new Servings(obj.servings().amount());
        json.created = obj.created();
        return json;
    }

    @Override
    public Recipe adaptFromJson(RecipeAnnotations obj) {
        return new Recipe(
                obj._id,
                obj.title,
                obj.preparation,
                Optional.ofNullable(obj.ingredients).map(ArrayList::new).orElse(new ArrayList<>()),
                Optional.ofNullable(obj.servings).map(servings -> new Servings(servings.amount())).orElse(new Servings(1)),
                0L);
    }
}
