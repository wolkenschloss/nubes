package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RecipeTest {

    @Test
    public void scaleRecipeTest() {

        var recipe = new Recipe(
                null,
                "Pure Water",
                "Fill pure water into a glass",
                List.of(new Ingredient(new Rational(300), "ml", "Water")),
                new Servings(3),
                0L);

        var scaled = recipe.scale(new Servings(4));

        var expected = new Recipe(
                null,
                "Pure Water",
                "Fill pure water into a glass",
                List.of(new Ingredient(new Rational(400), "ml", "Water")),
                new Servings(4),
                0L);

        Assertions.assertEquals(expected, scaled);
    }
}
