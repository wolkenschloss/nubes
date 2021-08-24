package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecipeTest {

    @Test
    public void scaleRecipeTest() {

        var recipe = new Recipe("Pure Water", "Fill pure water into a glass");
        recipe.ingredients.add(new Ingredient(new Rational(300), "ml", "Water"));
        recipe.servings = new Servings(3);

        var scaled = recipe.scale(new Servings(4));

        var expected = new Recipe("Pure Water", "Fill pure water into a glass");
        expected.ingredients.add(new Ingredient(new Rational(400), "ml", "Water"));
        expected.servings = new Servings(4);

        Assertions.assertEquals(expected, scaled);
    }
}
