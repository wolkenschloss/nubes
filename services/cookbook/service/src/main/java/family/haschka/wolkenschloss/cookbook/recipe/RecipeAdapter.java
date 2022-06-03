package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.Optional;

public class RecipeAdapter implements JsonbAdapter<Recipe, RecipeAnnotations> {
    @Override
    public RecipeAnnotations adaptToJson(Recipe obj) {
        ObjectId id = null;
        if (!obj.get_id().equals("unset")) {
            id = new ObjectId(obj.get_id());
        }

        var dto = new RecipeAnnotations();
        dto._id = id;
        dto.title = obj.getTitle();
        dto.preparation = obj.getPreparation();
        dto.ingredients = new ArrayList<>(obj.getIngredients());
        dto.servings = new Servings(obj.getServings().getAmount());
        dto.created = obj.getCreated();

        return dto;
    }

    @Override
    public Recipe adaptFromJson(RecipeAnnotations obj) {
        return new Recipe(
                Optional.ofNullable(obj._id).map(ObjectId::toHexString).orElse("unset"),
                obj.title,
                obj.preparation,
                Optional.ofNullable(obj.ingredients).orElse(new ArrayList<>()),
                Optional.ofNullable(obj.servings).orElse(new Servings(1)),
                Optional.ofNullable(obj.created).orElse(0L)
        );
    }
}
