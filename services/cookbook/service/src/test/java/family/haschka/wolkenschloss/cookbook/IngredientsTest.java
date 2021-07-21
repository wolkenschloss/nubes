package family.haschka.wolkenschloss.cookbook;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class IngredientsTest {

    @Test
    @DisplayName("New recipe should have no ingredients")
    public void testNewRecipe() {
        var recipe = new Recipe();

        Assertions.assertEquals(recipe.ingredients, Recipe.NoIngredients);
    }
}
