package family.haschka.wolkenschloss.cookbook.recipe;

import com.arjuna.ats.jta.exceptions.NotImplementedException;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.stream.Collectors;

public class RecipeAdapter implements JsonbAdapter<Recipe, RecipeAnnotated> {

    @Override
    public RecipeAnnotated adaptToJson(Recipe obj) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public Recipe adaptFromJson(RecipeAnnotated obj) {
        Recipe recipe = new Recipe(obj.name, obj.instruction);

        if (obj.ingredients != null) {
            recipe.ingredients = obj.ingredients.stream()
                    .map(Ingredient::parse)
                    .collect(Collectors.toList());
        }
        recipe.recipeId = null;
        return recipe;
    }
}
