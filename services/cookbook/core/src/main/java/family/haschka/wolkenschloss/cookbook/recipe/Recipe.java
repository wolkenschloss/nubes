package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Recipe(String _id, String title, String preparation, List<Ingredient> ingredients, Servings servings, Long created) {

    public Recipe {
        Objects.requireNonNull(ingredients);
        Objects.requireNonNull(servings);
        ingredients = new ArrayList<>(ingredients);
    }

    public Recipe(String title, String preparation) {
        this(null, title, preparation, new ArrayList<>(), new Servings(1), 0L);
    }

    @Override
    public int hashCode() {
        int result = _id != null ? _id.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (preparation != null ? preparation.hashCode() : 0);
        result = 31 * result + ingredients.hashCode();
        result = 31 * result + servings.hashCode();
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "recipeId=" + _id +
                ", title='" + title + '\'' +
                ", preparation='" + preparation + '\'' +
                ", ingredients=" + ingredients +
                ", servings=" + servings +
                '}';
    }

    public Recipe scale(Servings servings) {
        var factor = new Rational(servings.getAmount(), this.servings.getAmount());
        return new Recipe(
                this._id(),
                this.title(),
                this.preparation(),
                this.ingredients.stream().map(i -> i.scale(factor)).collect(Collectors.toCollection(ArrayList::new)),
                new Servings(servings.getAmount()),
                created());
    }
}
