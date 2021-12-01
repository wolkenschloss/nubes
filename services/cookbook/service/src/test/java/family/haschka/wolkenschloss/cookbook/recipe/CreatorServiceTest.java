package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.ingredient.IngredientRequiredEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;

@DisplayName("Creator Service")
public class CreatorServiceTest {

    RecipeRepository repository;
    IdentityGenerator generator;
    TimeService time;
    IngredientRequiredEventEmitter emitter;

    @BeforeEach
    public void mockCollaborators() {
        repository = Mockito.mock(RecipeRepository.class);
        generator = Mockito.mock(IdentityGenerator.class);
        time = Mockito.mock(TimeService.class);
        emitter = Mockito.mock(IngredientRequiredEventEmitter.class);
    }

    @Test
    @DisplayName("'save' should not lookup for ingredients, when failed")
    public void shouldNotLookupIngredients() {
        var failure = new RuntimeException("An error occurred");
        var recipe = RecipeFixture.LASAGNE.get();
        var recipeId = UUID.randomUUID();
        var now = ZonedDateTime.of(2021, 12, 1, 12, 31, 0, 0, ZoneId.systemDefault());

        Mockito.when(generator.generate())
                .thenReturn(recipeId);

        Mockito.when(repository.persist(RecipeFixture.LASAGNE.withId(recipeId)))
                .thenReturn(Uni.createFrom().failure(failure));

        Mockito.when(time.now())
                .thenReturn(now);

        CreatorService subjectUnderTest = new CreatorService(repository, generator, time, emitter);

        subjectUnderTest.save(recipe)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure()
                .assertFailedWith(failure.getClass(), failure.getMessage());

        Mockito.verify(repository, Mockito.times(1)).persist(recipe);
        Mockito.verify(generator, Mockito.times(1)).generate();
        Mockito.verify(time, Mockito.times(1)).now();
    }

    @Test
    @DisplayName("'save' should persist recipe entity")
    public void testShouldSaveRecipeWithoutIngredients() {

        var recipe = RecipeFixture.LASAGNE.get();
        var recipeId = UUID.randomUUID();
        var now = ZonedDateTime.of(2021, 12, 1, 12, 39, 0, 0, ZoneId.systemDefault());

        Mockito.when(generator.generate())
                .thenReturn(recipeId);

        Mockito.when(repository.persist(RecipeFixture.LASAGNE.withId(recipeId)))
                .thenReturn(Uni.createFrom().item(RecipeFixture.LASAGNE.withId(recipeId)));

        Mockito.when(time.now())
                .thenReturn(now);

        recipe.ingredients.forEach(ingredient -> Mockito.when(emitter.send(new IngredientRequiredEvent(recipeId, ingredient.name)))
                .thenReturn(CompletableFuture.allOf()));

        CreatorService subjectUnderTest = new CreatorService(repository, generator, time, emitter);

        subjectUnderTest.save(recipe)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(recipe);

        Mockito.verify(repository, Mockito.times(1)).persist(recipe);
        Mockito.verify(generator, Mockito.times(1)).generate();
        Mockito.verify(time, Mockito.times(1)).now();
        Mockito.verify(emitter, Mockito.times(recipe.ingredients.size())).send(any(IngredientRequiredEvent.class));
    }

    @AfterEach
    public void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(generator);
        Mockito.verifyNoMoreInteractions(time);
        Mockito.verifyNoMoreInteractions(emitter);
    }

    private interface IngredientRequiredEventEmitter extends Emitter<IngredientRequiredEvent> {
    }
}

