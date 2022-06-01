package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Function;

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

        var id = ObjectId.get();
        var recipe = Uni.createFrom().item(Optional.of(RecipeFixture.LASAGNE.withId(id.toHexString())));

        Mockito.when(recipeRepository.findByIdOptional(id))
                .thenReturn(recipe);

        var actual = subjectUnderTest
                .get(id.toHexString(), Optional.empty())
                .map(uni -> uni.orElseThrow(AssertionFailedError::new));

        var subscriber = actual.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(RecipeFixture.LASAGNE.withId(id.toHexString()));

        Mockito.verify(recipeRepository, Mockito.times(1)).findByIdOptional(id);
    }

    @ParameterizedTest
    @EnumSource(GetScaledRecipeTestcase.class)
    @DisplayName("'get' should scale servings")
    public void getShouldScaleServings(GetScaledRecipeTestcase testcase) {

        var recipe = testcase.recipe;

        Mockito.when(recipeRepository.findByIdOptional(new ObjectId(recipe._id())))
                .thenReturn(Uni.createFrom().item(Optional.of(recipe)));

        var actual = subjectUnderTest
                .get(recipe._id(), Optional.ofNullable(testcase.servings))
                .map(uni -> uni.orElseThrow(AssertionFailedError::new));

        var subscriber = actual.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(testcase.expected);

        Mockito.verify(recipeRepository, Mockito.times(1)).findByIdOptional(new ObjectId(recipe._id()));
    }

    @Test
    @DisplayName("'get' should not return value if missing'")
    public void testNewRecipe2() {

        var anotherId = ObjectId.get();

        Mockito.when(recipeRepository.findByIdOptional(anotherId))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        var recipe = subjectUnderTest
                .get(anotherId.toHexString(), Optional.empty());

        var subscriber = recipe.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(Optional.empty());

        Mockito.verify(recipeRepository, Mockito.times(1)).findByIdOptional(anotherId);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void deleteShouldReturnResult(boolean value) {
        var id = ObjectId.get();

        Mockito.when(recipeRepository.deleteById(id))
                .thenReturn(Uni.createFrom().item(value));

        var result = subjectUnderTest.delete(id.toHexString());
        var subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(value);

        Mockito.verify(recipeRepository, Mockito.times(1)).deleteById(id);
    }

    @Test
    public void canUpdateRecipe() {
        var recipe = RecipeFixture.LASAGNE.withId(ObjectId.get().toHexString());

        Mockito.when(recipeRepository.update(recipe))
                .thenReturn(Uni.createFrom().item(recipe));

        var result = subjectUnderTest.update(recipe);
        var subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(recipe);
        Mockito.verify(recipeRepository, Mockito.times(1)).update(recipe);
    }

    @Test
    @DisplayName("'save' can throw exception")
    public void testCanThrowException() {

    }

    enum GetScaledRecipeTestcase {
        LASAGNE(RecipeFixture.LASAGNE.withId(), new Servings(5), recipe -> recipe.scale(new Servings(5))),
        CHILI(RecipeFixture.CHILI_CON_CARNE.withId(), null, r -> r);

        private final Recipe recipe;
        private final Servings servings;
        private final Recipe expected;

        GetScaledRecipeTestcase(Recipe recipe, Servings servings, Function<Recipe, Recipe> expected) {
            this.recipe = recipe;
            this.servings = servings;
            this.expected = expected.apply(recipe);
        }
    }
}
