package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

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

        var id = UUID.randomUUID();
        var recipe = Uni.createFrom().item(Optional.of(RecipeFixture.LASAGNE.withId(id)));

        Mockito.when(recipeRepository.findByIdOptional(id))
                .thenReturn(recipe);

        var actual = subjectUnderTest
                .get(id, Optional.empty())
                .map(uni -> uni.orElseThrow(AssertionFailedError::new));

        var subscriber = actual.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(RecipeFixture.LASAGNE.withId(id));

        Mockito.verify(recipeRepository, Mockito.times(1)).findByIdOptional(id);
    }

    @ParameterizedTest
    @EnumSource(GetScaledRecipeTestcase.class)
    @DisplayName("'get' should scale servings")
    public void getShouldScaleServings(GetScaledRecipeTestcase testcase) {

        var recipe = testcase.recipe;

        Mockito.when(recipeRepository.findByIdOptional(recipe.recipeId))
                .thenReturn(Uni.createFrom().item(Optional.of(recipe)));

        var actual = subjectUnderTest
                .get(recipe.recipeId, Optional.ofNullable(testcase.servings))
                .map(uni -> uni.orElseThrow(AssertionFailedError::new));

        var subscriber = actual.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(testcase.expected);

        Mockito.verify(recipeRepository, Mockito.times(1)).findByIdOptional(recipe.recipeId);
    }

    @Test
    @DisplayName("'get' should not return value if missing'")
    public void testNewRecipe2() {

        var anotherId = UUID.randomUUID();

        Mockito.when(recipeRepository.findByIdOptional(anotherId))
                .thenReturn(Uni.createFrom().item(Optional.empty()));

        var recipe = subjectUnderTest
                .get(anotherId, Optional.empty());

        var subscriber = recipe.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(Optional.empty());

        Mockito.verify(recipeRepository, Mockito.times(1)).findByIdOptional(anotherId);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void deleteShouldReturnResult(boolean value) {
        var id = UUID.randomUUID();

        Mockito.when(recipeRepository.deleteById(id))
                .thenReturn(Uni.createFrom().item(value));

        var result = subjectUnderTest.delete(id);
        var subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(value);

        Mockito.verify(recipeRepository, Mockito.times(1)).deleteById(id);
    }

    @Test
    public void canUpdateRecipe() {
        var recipe = RecipeFixture.LASAGNE.withId(UUID.randomUUID());

        Mockito.when(recipeRepository.update(recipe))
                .thenReturn(Uni.createFrom().item(recipe));

        var result = subjectUnderTest.update(recipe);
        var subscriber = result.subscribe().withSubscriber(UniAssertSubscriber.create());
        subscriber.assertCompleted().assertItem(recipe);
        Mockito.verify(recipeRepository, Mockito.times(1)).update(recipe);
    }

    @Test
    @DisplayName("'list' should calculate recent updates")
    public void calculateRecentUpdate() {

        var recipes = Stream.of(
                        RecipeFixture.LASAGNE.withId(UUID.randomUUID()),
                        RecipeFixture.CHILI_CON_CARNE.withId(UUID.randomUUID()))
                .toList();

        RecipeQuery query = Mockito.mock(RecipeQuery.class);
        RecipeQuery page = Mockito.mock(RecipeQuery.class);


        Mockito.when(query.count())
                .thenReturn(Uni.createFrom().item((long) recipes.size()));

        Mockito.when(recipeRepository.findAll(any()))
                .thenReturn(query);

        Mockito.when(query.range(0, 10))
                .thenReturn(page);

        Mockito.when(page.list())
                .thenReturn(Uni.createFrom().item(recipes));

        var result = subjectUnderTest.list(0, 10, "");

        var expected = new TableOfContents(
                (long) recipes.size(),
                recipes.stream().map(recipe -> new Summary(recipe.recipeId, recipe.title)).toList()
        );

        result.subscribe().withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(expected);


        Mockito.verify(recipeRepository, Mockito.times(1)).findAll(any());
    }

    @Test
    @DisplayName("'save' can throw exception")
    public void testCanThrowException() {

    }

    enum GetScaledRecipeTestcase {
        LASAGNE(RecipeFixture.LASAGNE.get(), new Servings(5), RecipeFixture.LASAGNE.get().scale(new Servings(5))),
        CHILI(RecipeFixture.CHILI_CON_CARNE.get(), null, RecipeFixture.CHILI_CON_CARNE.get());

        private final Recipe recipe;
        private final Servings servings;
        private final Recipe expected;

        GetScaledRecipeTestcase(Recipe recipe, Servings servings, Recipe expected) {
            this.recipe = recipe;
            this.servings = servings;
            this.expected = expected;
        }
    }
}
