package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.job.EventBusAddress;
import family.haschka.wolkenschloss.cookbook.job.JobCreatedEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.eventbus.EventBus;
import io.vertx.mutiny.core.eventbus.Message;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class JobReceivedEventTest {

//    @InjectMock
//    RecipeRepository recipeRepository;
//
//    @InjectMock
//    JobService jobService;
//
//    @InjectMock
//    IdentityGenerator identityGenerator;
//
//    @Inject
//    EventBus bus;
//
//    @InjectMock
//    DataSource grabber;

    @Test
    @DisplayName("should import recipe from url")
    public void testImportRecipe() throws URISyntaxException {

        Logger log = Mockito.mock(Logger.class);
        RecipeRepository recipeRepository = Mockito.mock(RecipeRepository.class);
        IdentityGenerator identityGenerator = Mockito.mock(IdentityGenerator.class);
        EventBus eventBus = Mockito.mock(EventBus.class);
        DataSource dataSource = Mockito.mock(DataSource.class);

        RecipeFixture fixture = RecipeFixture.LASAGNE;
        var lasagneUri = fixture.getRecipeSource();
        var recipeId = UUID.randomUUID();

        Mockito.when(dataSource.extract(any(URI.class), any()))
                .thenReturn(fixture.toUni());

        Mockito.when(identityGenerator.generate())
                .thenReturn(recipeId);

        Mockito.when(recipeRepository.persist(any(Recipe.class)))
                .thenReturn(Uni.createFrom().item(fixture.withId(recipeId)));

        var event = new JobCreatedEvent(UUID.randomUUID(), lasagneUri);
        RecipeService service = new RecipeService(log, recipeRepository, identityGenerator, eventBus, dataSource);
        service.onJobCreated(event);
        var expected = UriBuilder.fromUri("/recipe/{id}").build(recipeId);

//        result.map(Message::body)
//                .subscribe().withSubscriber(UniAssertSubscriber.create())
//                .awaitItem()
//                .assertItem(expected);

        Mockito.verify(dataSource, Mockito.timeout(1000).times(1))
                .extract(eq(event.source()), any());

        Mockito.verify(recipeRepository, Mockito.timeout(1000).times(1))
                .persist(fixture.withId(recipeId));

        Mockito.verify(eventBus, Mockito.timeout(1000).times(1))
                        .send(EventBusAddress.IMPORTED, new RecipeImportedEvent(event.jobId(), expected));

        Mockito.verifyNoMoreInteractions(recipeRepository);
        Mockito.verifyNoMoreInteractions(dataSource);
    }
}
