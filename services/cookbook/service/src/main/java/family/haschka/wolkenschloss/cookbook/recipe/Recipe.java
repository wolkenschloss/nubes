package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.codecs.pojo.annotations.BsonId;

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
        this.recipeId = null;
        this.title = title;
        this.preparation = preparation;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId=" + recipeId +
                ", title='" + title + '\'' +
                ", preparation='" + preparation + '\'' +
                ", ingredients=" + ingredients +
                '}';
    }

    static List<Ingredient> NoIngredients = List.of();
}
