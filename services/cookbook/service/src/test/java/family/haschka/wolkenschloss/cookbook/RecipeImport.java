package family.haschka.wolkenschloss.cookbook;

import java.util.List;

public class RecipeImport {
    public RecipeImport(ResourceHtmlReader reader) {
    }

    public List<Recipe> extract() {
        var recipe = new Recipe();
        recipe.recipeId = null;
        recipe.title = "Lasagne";

        return List.of(recipe);
    }
}
