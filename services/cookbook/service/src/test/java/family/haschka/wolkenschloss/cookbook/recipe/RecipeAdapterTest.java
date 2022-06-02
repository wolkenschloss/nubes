package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RecipeAdapterTest {
    @Test
    public void toJsonTest() {
        var adapter = new RecipeAdapter();
        var annotated = new RecipeAnnotations();
        annotated.title = "Hello recipe";

        var recipe = adapter.adaptFromJson(annotated);

        Assertions.assertNotNull(recipe);
    }
}
