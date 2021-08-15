package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.job.JobCompletedEvent;
import family.haschka.wolkenschloss.cookbook.job.JobReceivedEvent;
import family.haschka.wolkenschloss.cookbook.job.JobService;
import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.Observes;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class JobReceivedEventTest {

    @InjectMock
    RecipeRepository recipeRepository;

    // Verhindert, dass JobService das Ereignis JobCompletedEvent empfängt.
    @SuppressWarnings("unused")
    @InjectMock
    JobService jobService;

    @Inject
    Event<JobReceivedEvent> received;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    ManagedExecutor executor;

    @Test
    @DisplayName("should import recipe from url")
    public void testImportRecipe() throws ExecutionException, InterruptedException, URISyntaxException {

        var lasagneUri = this.getClass().getClassLoader().getResource("lasagne.html").toURI();
        var recipeId = UUID.randomUUID();

        Mockito.doAnswer(recipe -> {
            recipe.getArgument(0, Recipe.class).recipeId = recipeId;
            return null;
        }).when(recipeRepository).persist(any(Recipe.class));

        var event = new JobReceivedEvent(UUID.randomUUID(), lasagneUri);

        received.fireAsync(event, NotificationOptions.ofExecutor(executor))
                .thenAccept(e -> {

                    var expectedUri = UriBuilder.fromUri("/recipe/{id}").build(recipeId);
                    var expectedEvent = new JobCompletedEvent(e.jobId(), expectedUri, null);
                    Assertions.assertTrue(observer.getEvents().contains(expectedEvent));
                })
                .toCompletableFuture().get();

        Mockito.verify(recipeRepository, Mockito.times(1)).persist(any(Recipe.class));
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
}
