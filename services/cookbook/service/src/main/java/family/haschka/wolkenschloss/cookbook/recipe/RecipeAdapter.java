package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.Optional;

public class RecipeAdapter implements JsonbAdapter<Recipe, RecipeAnnotations> {
    @Override
    public RecipeAnnotations adaptToJson(Recipe obj) {
        var json = new RecipeAnnotations();
        json._id = Optional.ofNullable(obj._id()).map(ObjectId::new).orElse(null);
        json.title = obj.title();
        json.preparation = obj.preparation();
        json.ingredients = new ArrayList<>(obj.ingredients());
        json.servings = new Servings(obj.servings().getAmount());
        json.created = obj.created();
        return json;
    }

    @Override
    public Recipe adaptFromJson(RecipeAnnotations obj) {
        return new Recipe(
                Optional.ofNullable(obj._id).map(ObjectId::toHexString).orElse(null),
                obj.title,
                obj.preparation,
                Optional.ofNullable(obj.ingredients).map(ArrayList::new).orElse(new ArrayList<>()),
                Optional.ofNullable(obj.servings).map(servings -> new Servings(servings.getAmount())).orElse(new Servings(1)),
                0L);
    }
}
