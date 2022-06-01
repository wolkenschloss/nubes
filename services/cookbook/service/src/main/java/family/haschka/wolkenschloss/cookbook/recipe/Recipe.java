package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record Recipe(ObjectId _id, String title, String preparation, List<Ingredient> ingredients, Servings servings, Long created) {

    public Recipe {
        Objects.requireNonNull(ingredients);
        Objects.requireNonNull(servings);
    }
//    public ArrayList ingredients = new ArrayList<>();

    public Recipe(String title, String preparation) {
        this(null, title, preparation, new ArrayList<>(), new Servings(1), 0L);
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
        var factor = new Rational(servings.amount(), this.servings.amount());
        return new Recipe(
                this._id(),
                this.title(),
                this.preparation(),
                this.ingredients.stream().map(i -> i.scale(factor)).collect(Collectors.toCollection(ArrayList::new)),
                new Servings(servings.amount()),
                created());
    }
}
