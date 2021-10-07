package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class RecipeImportTest {

    @ParameterizedTest
    @EnumSource(RecipeFixture.class)
    @DisplayName("should convert ld+json script to recipe")
    public void convertLdJsonScriptTest(RecipeFixture testcase) throws IOException, URISyntaxException {
        var importer = new RecipeImport();
        List<Recipe> recipes = importer.extract(testcase.read());

        Assertions.assertEquals(1, recipes.size());
        Assertions.assertEquals(testcase.get(), recipes.get(0));
    }
}
