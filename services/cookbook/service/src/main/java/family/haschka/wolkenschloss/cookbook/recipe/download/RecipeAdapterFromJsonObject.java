package family.haschka.wolkenschloss.cookbook.recipe.download;

import com.arjuna.ats.jta.exceptions.NotImplementedException;
import family.haschka.wolkenschloss.cookbook.recipe.Ingredient;
import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.Servings;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// Alternative zu RecipeAdapterFromRecipeAnnotated. Hier besteht die volle Kontrolle
// über die Serialisierung, ist dafür aber etwas umständlicher, aber erfordert keine
// annotierte Adapterklasse.
//
// Die Klasse wird derzeit nicht verwendet, ist aber als Dokumentation und Blaupause
// hilfreich.
@SuppressWarnings("unused")
public class RecipeAdapterFromJsonObject implements JsonbAdapter<Recipe, JsonObject> {

    @Override
    public JsonObject adaptToJson(Recipe obj) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public Recipe adaptFromJson(JsonObject obj) {
        return new Recipe(
                "unset",
                obj.getString("name"),
                obj.getString("recipeInstructions"),
                Optional.ofNullable(obj.getJsonArray("recipeIngredient"))
                    .map(array -> array.getValuesAs(f -> ((JsonString) f).getString())
                        .stream()
                        .map(Ingredient::parse)
                        .collect(Collectors.toCollection(ArrayList::new)))
                    .orElse(new ArrayList<>()),
                new Servings(obj.getInt("recipeYield", 1)),
                0L);
    }
}
