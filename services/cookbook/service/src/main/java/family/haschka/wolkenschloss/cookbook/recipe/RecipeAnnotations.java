package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;

import javax.json.JsonArray;

public class RecipeAnnotations {
    public ObjectId _id;
    public String title;
    public String preparation;
    public JsonArray ingredients;
    public Servings servings;
    public Long created;
}
