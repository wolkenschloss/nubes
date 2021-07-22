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
    RecipeService recipeService;

    Recipe subjectUnderTest = null;

    @BeforeEach
    public void recipeWithNoIngredients() {
        subjectUnderTest = new Recipe("Luft", "Zum Leben brauche ich nur Luft und Liebe.");
        subjectUnderTest.recipeId = UUID.randomUUID();
    }

    @AfterEach
    public void verifyMockInteractions() {
        Mockito.verifyNoMoreInteractions(recipeRepository);
    }
    @Test
    @DisplayName("'get' should return value if exists")
    public void testNewRecipe() {

        Assertions.assertNotNull(subjectUnderTest);
        Assertions.assertNotNull(subjectUnderTest.recipeId);


        Mockito.when(recipeRepository.findById(subjectUnderTest.recipeId))
                .thenReturn(subjectUnderTest);


         var recipe = recipeService
                .get(subjectUnderTest.recipeId)
                .orElseThrow(AssertionFailedError::new);

         Assertions.assertEquals(recipe, subjectUnderTest);

        Mockito.verify(recipeRepository, Mockito.times(1)).findById(subjectUnderTest.recipeId);

    }

    @Test
    @DisplayName("'get' should not return value if missing'")
    public void testNewRecipe2() {

        Assertions.assertNotNull(subjectUnderTest);
        Assertions.assertNotNull(subjectUnderTest.recipeId);

        var anotherId = UUID.randomUUID();

        Mockito.when(recipeRepository.findById(anotherId))
                .thenReturn(null);


        var recipe = recipeService
                .get(anotherId);

        Assertions.assertEquals(recipe.isPresent(), false);

        Mockito.verify(recipeRepository, Mockito.times(1)).findById(anotherId);
    }

    @Test
    public void testShouldSaveRecipeWithoutIngredients() {
        var recipe = new Recipe();
        try {
            recipeService.save(recipe);
            Assertions.assertTrue(true);
        } catch (Exception e) {
            Assertions.fail();
        }

        Mockito.verify(recipeRepository, Mockito.times(1)).persist(recipe);
    }
}
