package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeImport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class RecipeImportTest {

    public enum Testcase {
        LASAGNE("lasagne.html", "Lasagne"),
        CHILI_CON_CARNE("chili.html", "Chili con carne");

        private final String filename;
        private final String name;

        Testcase(String filename, String name) {
            this.filename = filename;
            this.name = name;
        }

        public URI getRecipeSource() throws URISyntaxException {
            var uri = this.getClass().getClassLoader().getResource(filename);
            return uri.toURI();
        }
    }

    @ParameterizedTest
    @EnumSource
    @DisplayName("should convert ld+json script to recipe")
    public void xxx(Testcase testcase) throws IOException, URISyntaxException {
        var importer = new RecipeImport();
        List<Recipe> recipes = importer.extract(testcase.getRecipeSource());

        Assertions.assertEquals(1, recipes.size());
        Assertions.assertEquals(testcase.name, recipes.get(0).title);
    }
}
