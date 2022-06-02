package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.Optional;

public class RecipeAdapter implements JsonbAdapter<Recipe, RecipeAnnotations> {
    @Override
    public RecipeAnnotations adaptToJson(Recipe obj) {
        var dto = new RecipeAnnotations();
        dto._id = Optional.ofNullable(obj._id()).map(ObjectId::new).orElse(null);
        dto.title = obj.title();
        dto.preparation = obj.preparation();
        dto.ingredients = new ArrayList<>(obj.ingredients());
        dto.servings = new Servings(obj.servings().getAmount());
        dto.created = obj.created();

        return dto;
    }

    @Override
    public Recipe adaptFromJson(RecipeAnnotations obj) {
        return new Recipe(
                Optional.ofNullable(obj._id).map(ObjectId::toHexString).orElse(null),
                obj.title,
                obj.preparation,
                Optional.ofNullable(obj.ingredients).orElse(new ArrayList<>()),
                Optional.ofNullable(obj.servings).orElse(new Servings(1)),
                Optional.ofNullable(obj.created).orElse(0L)
        );
    }
}
