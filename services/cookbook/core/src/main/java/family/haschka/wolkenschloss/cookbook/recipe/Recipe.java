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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Recipe recipe = (Recipe) o;

        if (_id != null ? !_id.equals(recipe._id) : recipe._id != null) return false;
        if (title != null ? !title.equals(recipe.title) : recipe.title != null) return false;
        if (preparation != null ? !preparation.equals(recipe.preparation) : recipe.preparation != null) return false;

        if (!ingredients.containsAll(recipe.ingredients) || !recipe.ingredients.containsAll(ingredients)) {
            return false;
        }

        if (!servings.equals(recipe.servings)) return false;
        return created != null ? created.equals(recipe.created) : recipe.created == null;
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
