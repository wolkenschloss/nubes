package family.haschka.wolkenschloss.cookbook;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class RecipeServiceTest {

    @InjectMock
    RecipeRepository recipeRepository;

    @InjectMock
    ResourceParser parser;

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

    @Inject
    Event<JobReceivedEvent> received;

    @Test
    @DisplayName("should import recipe from url")
    public void testImportRecipe() throws ExecutionException, InterruptedException, IOException {
        var event = new JobReceivedEvent();
        event.jobId = UUID.randomUUID();
        event.source = URI.create("http://meinerezepte.local/lasagne.html");

        var recipeId = UUID.randomUUID();
        var lasagne = new Recipe();
        lasagne.recipeId = null;
        lasagne.title = "Lasagne";

        var localParser = new ResourceHtmlParser();
        var data = localParser.readData(URI.create("lasagne.html"));
        Mockito.when(parser.readData(event.source)).thenReturn(data);
        Mockito.doAnswer(recipe -> {
            recipe.getArgument(0, Recipe.class).recipeId = recipeId;
            return null;
        }).when(recipeRepository).persist(any(Recipe.class));

        var future = received.fireAsync(event);
        future.thenAccept(e -> {
            var expected = new JobCompletedEvent();
            expected.error = Optional.empty();
            expected.jobId = e.jobId;
            expected.location = Optional.of(UriBuilder.fromUri(URI.create("/recipe"))
                    .path(recipeId.toString()).build());

            Assertions.assertTrue(observer.getEvents().contains(expected));

        }).toCompletableFuture().get();

        Mockito.verify(parser, Mockito.times(1)).readData(event.source);
        Mockito.verify(recipeRepository, Mockito.times(1)).persist(any(Recipe.class));

        Mockito.verifyNoMoreInteractions(parser);
        Mockito.verifyNoMoreInteractions(recipeRepository);
    }

    @Inject
    JobCompletedObserver observer;

    @Singleton
    static class JobCompletedObserver {

        private List<JobCompletedEvent> events;

        @PostConstruct
        void init() {
            events = new CopyOnWriteArrayList<>();
        }

        void observeAsync(@ObservesAsync JobCompletedEvent event) {
            events.add(event);
        }

        void observeSync(@Observes JobCompletedEvent event) {
            events.add(event);
        }
        List<JobCompletedEvent> getEvents() {
            return events;
        }
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
