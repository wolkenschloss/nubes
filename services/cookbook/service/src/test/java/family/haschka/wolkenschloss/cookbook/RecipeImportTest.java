package family.haschka.wolkenschloss.cookbook;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

public class RecipeImportTest {

    public enum Testcase {
        LASAGNE("recipe.html", "Lasagne");

        private final String filename;
        private final String name;

        Testcase(String filename, String name) {
            this.filename = filename;
            this.name = name;
        }
    }

    @ParameterizedTest
    @EnumSource
    public void xxx(Testcase testcase) {
        var reader = new ResourceHtmlReader(testcase.filename);
        var importer = new RecipeImport(reader);
        List<Recipe> recipes = importer.extract();

        Assertions.assertEquals(recipes.size(), 1);
        Assertions.assertEquals(recipes.get(0).title, testcase.name);
    }
}
