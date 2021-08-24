package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Recipe {
    @BsonId
    public UUID recipeId;
    public String title;
    public String preparation;
    public ArrayList<Ingredient> ingredients = new ArrayList<>();
    public Servings servings;

    protected Recipe() {
        this(null, null);
    }

    public Recipe(String title, String preparation) {
        this.recipeId = null;
        this.title = title;
        this.preparation = preparation;
        this.servings = new Servings(1);
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId=" + recipeId +
                ", title='" + title + '\'' +
                ", preparation='" + preparation + '\'' +
                ", ingredients=" + ingredients +
                ", servings=" + servings +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(recipeId, recipe.recipeId) && Objects.equals(title, recipe.title) && Objects.equals(preparation, recipe.preparation) && Objects.equals(ingredients, recipe.ingredients) && Objects.equals(servings, recipe.servings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipeId, title, preparation, ingredients, servings);
    }

    public Recipe scale(Servings servings) {
        var result = new Recipe(this.title, this.preparation);
        result.recipeId = this.recipeId;
        result.servings = servings;

        var factor = new Rational(servings.amount(), this.servings.amount());

        result.ingredients = this.ingredients.stream()
                .map(i -> i.scale(factor))
                .collect(Collectors.toCollection(ArrayList::new));

        return result;
    }
}
