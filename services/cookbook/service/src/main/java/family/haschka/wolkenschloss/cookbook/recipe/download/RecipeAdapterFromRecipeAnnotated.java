package family.haschka.wolkenschloss.cookbook.recipe.download;

import com.arjuna.ats.jta.exceptions.NotImplementedException;
import family.haschka.wolkenschloss.cookbook.recipe.Ingredient;
import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.Servings;

import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class RecipeAdapterFromRecipeAnnotated implements JsonbAdapter<Recipe, RecipeAnnotated> {

    @Override
    public  RecipeAnnotated adaptToJson(Recipe obj) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public Recipe adaptFromJson( RecipeAnnotated obj) {
        return new Recipe(
                null,
                obj.name,
                obj.instruction,
                Optional.ofNullable(obj.ingredients)
                    .map(list -> list.stream()
                        .map(Ingredient::parse)
                        .collect(Collectors.toCollection(ArrayList::new)))
                    .orElse(new ArrayList<>()),
                Optional.ofNullable(obj.servings).orElse(new Servings(1)),
                0L);
    }
}
