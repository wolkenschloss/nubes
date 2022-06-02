package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RecipeTest {

    @Test
    public void RecipeEqualsTest() {
        var recipe = new Recipe(
                "42",
                "Rezept 42",
                "Zubereitung",
                List.of(new Ingredient(new Rational(2), Unit.MILLIGRAM.getUnit(), "Mondzucker")),
                new Servings(1),
                0L);

        var scaled1 = recipe.scale(new Servings(2));
        var scaled2 = recipe.scale(new Servings(2));

        Assertions.assertEquals(scaled1, scaled2);
    }

    @Test
    public void scaleRecipeTest() {

        var recipe = new Recipe(
                "unset",
                "Pure Water",
                "Fill pure water into a glass",
                List.of(new Ingredient(new Rational(300), "ml", "Water")),
                new Servings(3),
                0L);

        var scaled = recipe.scale(new Servings(4));

        var expected = new Recipe(
                "unset",
                "Pure Water",
                "Fill pure water into a glass",
                List.of(new Ingredient(new Rational(400), "ml", "Water")),
                new Servings(4),
                0L);

        Assertions.assertEquals(expected, scaled);
    }
}
