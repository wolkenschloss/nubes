package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;

import java.util.List;

public class RecipeAnnotations {
    public ObjectId _id;
    public String title;
    public String preparation;
    public List<Ingredient> ingredients;
    public Servings servings;
    public Long created;
}
