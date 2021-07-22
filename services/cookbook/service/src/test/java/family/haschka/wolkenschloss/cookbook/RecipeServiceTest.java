package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import javax.inject.Inject;

import java.util.UUID;

@QuarkusTest
public class RecipeServiceTest {

    @InjectMock
    RecipeRepository recipeRepository;

    @Inject
    RecipeService subjectUnderTest;

    Recipe recipe = null;

    @BeforeEach
    public void recipeWithNoIngredients() {
        recipe = new Recipe("Luft", "Zum Leben brauche ich nur Luft und Liebe.");
        recipe.recipeId = UUID.randomUUID();
    }

    @AfterEach
    public void verifyMockInteractions() {
        Mockito.verifyNoMoreInteractions(recipeRepository);
    }
    @Test
    @DisplayName("'get' should return value if exists")
    public void testNewRecipe() {

        Assertions.assertNotNull(recipe);
        Assertions.assertNotNull(recipe.recipeId);


        Mockito.when(recipeRepository.findById(recipe.recipeId))
                .thenReturn(recipe);


         var recipe = subjectUnderTest
                .get(this.recipe.recipeId)
                .orElseThrow(AssertionFailedError::new);

         Assertions.assertEquals(recipe, this.recipe);

        Mockito.verify(recipeRepository, Mockito.times(1)).findById(this.recipe.recipeId);

    }

    @Test
    @DisplayName("'get' should not return value if missing'")
    public void testNewRecipe2() {

        Assertions.assertNotNull(recipe);
        Assertions.assertNotNull(recipe.recipeId);

        var anotherId = UUID.randomUUID();

        Mockito.when(recipeRepository.findById(anotherId))
                .thenReturn(null);


        var recipe = subjectUnderTest
                .get(anotherId);

        Assertions.assertFalse(recipe.isPresent());

        Mockito.verify(recipeRepository, Mockito.times(1)).findById(anotherId);
    }

    @Test
    @DisplayName("'save' should persist recipe Entity")
    public void testShouldSaveRecipeWithoutIngredients() {
        try {
            subjectUnderTest.save(recipe);
            Assertions.assertTrue(true);
        } catch (Exception e) {
            Assertions.fail();
        }

        Mockito.verify(recipeRepository, Mockito.times(1)).persist(recipe);
    }

    @Test
    @DisplayName("'save' can throw exception")
    public void testCanThrowException() {

    }
}
