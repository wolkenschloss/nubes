package family.haschka.wolkenschloss.cookbook.recipe;

import com.arjuna.ats.jta.exceptions.NotImplementedException;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.bind.adapter.JsonbAdapter;
import java.util.ArrayList;
import java.util.List;
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
    public  JsonObject adaptToJson(Recipe obj) throws Exception {
        throw new NotImplementedException();
    }

    @Override
    public Recipe adaptFromJson( JsonObject obj) {
        var name = obj.getString("name");
        var preparation = obj.getString("recipeInstructions");
        var servings = obj.getInt("recipeYield", 1);

       Recipe recipe = new Recipe(name, preparation);
       recipe._id = null;
       recipe.servings = new Servings(servings);

       if (obj.containsKey("recipeIngredient")) {
           List<String> ingredients = obj.getJsonArray("recipeIngredient").getValuesAs(f -> ((JsonString) f).getString());

           if (ingredients != null) {
               recipe.ingredients = ingredients.stream()
                       .peek(System.out::println)
                       .map(Ingredient::parse)
                       .collect(Collectors.toCollection(ArrayList::new));
           }
       }

       return recipe;
    }
}
