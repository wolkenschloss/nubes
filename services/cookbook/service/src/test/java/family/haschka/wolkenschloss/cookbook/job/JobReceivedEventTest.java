package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.PostConstruct;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    ManagedExecutor executor;

    @Inject
    EventBus bus;

    @InjectMock
    DataGrabber grabber;

    @Test
    @DisplayName("should import recipe from url")
    public void testImportRecipe() throws InterruptedException, URISyntaxException, IOException {

        var lasagneUri = RecipeFixture.LASAGNE.getRecipeSource();
        var recipeId = UUID.randomUUID();

        Mockito.when(grabber.grab(any(URL.class)))
                .thenReturn(RecipeFixture.LASAGNE.toUni());

        Mockito.when(identityGenerator.generate())
                .thenReturn(recipeId);

        Mockito.when(recipeRepository.persist(any(Recipe.class)))
                .thenReturn(Uni.createFrom().item(RecipeFixture.LASAGNE.withId(recipeId)));

        var event = new JobReceivedEvent(UUID.randomUUID(), lasagneUri);

        bus.publish("job-received", event);

        var expected = new JobCompletedEvent(
                event.jobId(),
                UriBuilder.fromUri("/recipe/{id}").build(recipeId),
                null);

        Assertions.assertEquals(observer.lastEvent(), expected);

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

        @ConsumeEvent("job.completed")
        void observeAsync(@ObservesAsync JobCompletedEvent event) {
            events.offer(event);
        }

        JobCompletedEvent lastEvent() throws InterruptedException {
            return events.take();
        }
    }
}
