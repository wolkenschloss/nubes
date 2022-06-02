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

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;

@DisplayName("Creator Service")
public class CreatorServiceTest {

    RecipeRepository repository;
    IdentityGenerator generator;
    IngredientRequiredEventEmitter emitter;

    @BeforeEach
    public void mockCollaborators() {
        repository = Mockito.mock(RecipeRepository.class);
        generator = Mockito.mock(IdentityGenerator.class);
        emitter = Mockito.mock(IngredientRequiredEventEmitter.class);
    }

    @Test
    @DisplayName("'save' should not lookup for ingredients, when failed")
    public void shouldNotLookupIngredients() {
        var failure = new RuntimeException("An error occurred");
        var recipe = RecipeFixture.LASAGNE.get();
        var recipeWithId = RecipeFixture.LASAGNE.withId();

        Mockito.when(generator.generateObjectId())
                .thenReturn(recipeWithId.get_id());

        Mockito.when(repository.persist(recipeWithId))
                .thenReturn(Uni.createFrom().failure(failure));

        CreatorService subjectUnderTest = new CreatorService(repository, generator, emitter);

        subjectUnderTest.save(recipe)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitFailure()
                .assertFailedWith(failure.getClass(), failure.getMessage());

        Mockito.verify(repository, Mockito.times(1)).persist(recipeWithId);
        Mockito.verify(generator, Mockito.times(1)).generateObjectId();
    }

    @Test
    @DisplayName("'save' should persist recipe entity")
    public void testShouldSaveRecipeWithoutIngredients() {

        var recipe = RecipeFixture.LASAGNE.get();
        var recipeWithId = RecipeFixture.LASAGNE.withId();

        Mockito.when(generator.generateObjectId())
                .thenReturn(recipeWithId.get_id());

        Mockito.when(repository.persist(recipeWithId))
                .thenReturn(Uni.createFrom().item(recipeWithId));

        recipe.getIngredients().forEach(ingredient -> Mockito.when(
                        emitter.send(new IngredientRequiredEvent(recipeWithId.get_id(), ingredient.getName())))
                .thenReturn(CompletableFuture.allOf()));

        CreatorService subjectUnderTest = new CreatorService(repository, generator, emitter);

        subjectUnderTest.save(recipe)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(recipeWithId);

        Mockito.verify(repository, Mockito.times(1)).persist(recipeWithId);
        Mockito.verify(generator, Mockito.times(1)).generateObjectId();
        Mockito.verify(emitter, Mockito.times(recipe.getIngredients().size())).send(any(IngredientRequiredEvent.class));
    }

    @AfterEach
    public void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(repository);
        Mockito.verifyNoMoreInteractions(generator);
        Mockito.verifyNoMoreInteractions(emitter);
    }

    private interface IngredientRequiredEventEmitter extends Emitter<IngredientRequiredEvent> {
    }
}

