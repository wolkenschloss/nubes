package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.annotation.JsonbProperty;
import java.util.List;

public class RecipeAnnotated {

    @JsonbProperty("name")
    public String name;

    public String description;

    @JsonbProperty("recipeInstructions")
    public String instruction;

    @JsonbProperty("recipeIngredient")
    public List<String> ingredients;

    @JsonbProperty("recipeYield")
    public Servings servings;
}
