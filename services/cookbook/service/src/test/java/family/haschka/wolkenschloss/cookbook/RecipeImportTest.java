package family.haschka.wolkenschloss.cookbook;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RecipeImportTest {

    @Test
    public void xxx() {
        var reader = new ResourceHtmlReader("recipe.html");
        var importer = new RecipeImport(reader);
        List<Recipe> recipes = importer.extract();

        Assertions.assertEquals(recipes.size(), 1);
    }
}
