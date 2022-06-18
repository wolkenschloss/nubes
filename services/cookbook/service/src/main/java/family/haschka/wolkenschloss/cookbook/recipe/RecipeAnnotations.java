package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;

import javax.json.bind.annotation.JsonbProperty;
import java.net.URI;
import java.util.List;

public class RecipeAnnotations {
    @JsonbProperty(nillable = true)
    public ObjectId _id;
    public String title;
    public String preparation;
    public List<Ingredient> ingredients;
    public Servings servings;
    public Long created;
    public URI image;
}
