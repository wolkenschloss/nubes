package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.*;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class JobReceivedEventTest {

    @InjectMock
    RecipeRepository recipeRepository;

    @InjectMock
    JobService jobService;

    @InjectMock
    IdentityGenerator identityGenerator;

    @Inject
    EventBus bus;

    @InjectMock
    DataGrabber grabber;

    @Inject
    Logger log;

    @Test
    @DisplayName("should import recipe from url")
    public void testImportRecipe() throws URISyntaxException, IOException {

        var lasagneUri = RecipeFixture.LASAGNE.getRecipeSource();
        var recipeId = UUID.randomUUID();

        Mockito.when(grabber.grab(any(URL.class)))
                .thenReturn(RecipeFixture.LASAGNE.toUni());

        Mockito.when(identityGenerator.generate())
                .thenReturn(recipeId);

        Mockito.when(recipeRepository.persist(any(Recipe.class)))
                .thenReturn(Uni.createFrom().item(RecipeFixture.LASAGNE.withId(recipeId)));

        var event = new JobReceivedEvent(UUID.randomUUID(), lasagneUri);

        bus.send("job-received", event);

        var expected = new JobCompletedEvent(
                event.jobId(),
                UriBuilder.fromUri("/recipe/{id}").build(recipeId),
                null);

        Mockito.verify(recipeRepository, Mockito.timeout(1000).times(1))
                .persist(RecipeFixture.LASAGNE.withId(recipeId));

        Mockito.verify(jobService, Mockito.timeout(1000).times(1))
                        .onCompleted(expected);

        Mockito.verifyNoMoreInteractions(recipeRepository);
        Mockito.verifyNoMoreInteractions(jobService);
    }
}
