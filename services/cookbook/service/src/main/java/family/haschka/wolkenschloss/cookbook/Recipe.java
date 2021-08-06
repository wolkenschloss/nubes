package family.haschka.wolkenschloss.cookbook;

import org.bson.codecs.pojo.annotations.BsonId;

import javax.json.bind.annotation.JsonbProperty;
import java.util.List;
import java.util.UUID;

public class Recipe {
    @BsonId
    public UUID recipeId;


    public String title;
    public String preparation;
    public List<Ingredient> ingredients = NoIngredients;

    protected Recipe() {}

    public Recipe(String title, String preparation) {

        this.title = title;
        this.preparation = preparation;
    }

    static List<Ingredient> NoIngredients = List.of();
}
