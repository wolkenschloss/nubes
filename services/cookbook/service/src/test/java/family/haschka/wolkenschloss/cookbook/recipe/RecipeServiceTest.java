package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

@QuarkusTest
public class RecipeServiceTest {

    @InjectMock
    RecipeRepository recipeRepository;

    @Inject
    RecipeService subjectUnderTest;

    @AfterEach
    public void verifyMockInteractions() {
        Mockito.verifyNoMoreInteractions(recipeRepository);
    }

    @Test
    @DisplayName("'get' should return value if exists")
    public void testNewRecipe() {

        var recipe = RecipeFixture.LASAGNE.withId(UUID.randomUUID());

        Mockito.when(recipeRepository.findById(recipe.recipeId))
                .thenReturn(recipe);

        var actual = subjectUnderTest
                .get(recipe.recipeId, Optional.empty())
                .orElseThrow(AssertionFailedError::new);

        Assertions.assertEquals(recipe, actual);

        Mockito.verify(recipeRepository, Mockito.times(1)).findById(recipe.recipeId);
    }

    enum GetScaledRecipeTestcase {
        LASAGNE(RecipeFixture.LASAGNE.recipe, new Servings(5), RecipeFixture.LASAGNE.recipe.scale(new Servings(5))),
        CHILI(RecipeFixture.CHILI_CON_CARNE.recipe, null, RecipeFixture.CHILI_CON_CARNE.recipe);

        private final Recipe recipe;
        private final Servings servings;
        private final Recipe expected;

        GetScaledRecipeTestcase(Recipe recipe, Servings servings, Recipe expected) {
            this.recipe = recipe;
            this.servings = servings;
            this.expected = expected;
        }
    }

    @ParameterizedTest
    @EnumSource(GetScaledRecipeTestcase.class)
    @DisplayName("'get' should scale servings")
    public void getShouldScaleServings(GetScaledRecipeTestcase testcase) {

        var recipe = testcase.recipe;

        Mockito.when(recipeRepository.findById(recipe.recipeId))
                .thenReturn(recipe);

        var actual = subjectUnderTest
                .get(recipe.recipeId, Optional.ofNullable(testcase.servings))
                .orElseThrow(AssertionFailedError::new);

        Assertions.assertEquals(testcase.expected, actual);

        Mockito.verify(recipeRepository, Mockito.times(1)).findById(recipe.recipeId);
    }

    @Test
    @DisplayName("'get' should not return value if missing'")
    public void testNewRecipe2() {

        var anotherId = UUID.randomUUID();

        Mockito.when(recipeRepository.findById(anotherId))
                .thenReturn(null);

        var recipe = subjectUnderTest
                .get(anotherId, Optional.empty());

        Assertions.assertFalse(recipe.isPresent());

        Mockito.verify(recipeRepository, Mockito.times(1)).findById(anotherId);
    }

    @Test
    @DisplayName("'save' should persist recipe Entity")
    public void testShouldSaveRecipeWithoutIngredients() {
        var recipe = RecipeFixture.LASAGNE.withId(UUID.randomUUID());

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
