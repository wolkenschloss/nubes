package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.IdentityGenerator;
import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeFixture;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class JobReceivedEventTest {

    @InjectMock
    RecipeRepository recipeRepository;

    // Verhindert, dass JobService das Ereignis JobCompletedEvent empfängt.
    @SuppressWarnings("unused")
    @InjectMock
    JobService jobService;

    @InjectMock
    IdentityGenerator identityGenerator;

    @Inject
    Event<JobReceivedEvent> received;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    ManagedExecutor executor;

    @Test
    @DisplayName("should import recipe from url")
    public void testImportRecipe() throws InterruptedException, URISyntaxException {

        var lasagneUri = RecipeFixture.LASAGNE.getRecipeSource();
        var recipeId = UUID.randomUUID();

        Mockito.when(identityGenerator.generate())
                .thenReturn(recipeId);

        Mockito.when(recipeRepository.persist(any(Recipe.class)))
                .thenReturn(Uni.createFrom().item(RecipeFixture.LASAGNE.withId(recipeId)));

        var event = new JobReceivedEvent(UUID.randomUUID(), lasagneUri);

        var expected = Uni.createFrom().completionStage(
                        received.fireAsync(event, NotificationOptions.ofExecutor(executor)))
                .map(received -> new JobCompletedEvent(
                        received.jobId(),
                        UriBuilder.fromUri("/recipe/{id}").build(recipeId),
                        null));

        var subscriber = expected.subscribe().withSubscriber(UniAssertSubscriber.create());

        Assertions.assertEquals(observer.lastEvent(), subscriber.awaitItem().getItem());

        Mockito.verify(recipeRepository, Mockito.times(1)).persist(any(Recipe.class));
        Mockito.verifyNoMoreInteractions(recipeRepository);
    }

    @Inject
    JobCompletedObserver observer;

    @Singleton
    static class JobCompletedObserver {

        private SynchronousQueue<JobCompletedEvent> events;

        @PostConstruct
        void init() {
            events = new SynchronousQueue<>();
        }

        void observeAsync(@ObservesAsync JobCompletedEvent event) {
            events.offer(event);
        }

        JobCompletedEvent lastEvent() throws InterruptedException {
            return events.take();
        }
    }
}
