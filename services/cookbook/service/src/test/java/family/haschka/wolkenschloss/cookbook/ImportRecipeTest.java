package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.job.IJobService;
import family.haschka.wolkenschloss.cookbook.job.ImportJobRepository;
import family.haschka.wolkenschloss.cookbook.job.JobCompletedEvent;
import family.haschka.wolkenschloss.cookbook.job.JobReceivedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeRepository;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeService;
import family.haschka.wolkenschloss.cookbook.recipe.ResourceParser;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
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
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
@TestProfile(MockJobServiceProfile.class)
public class ImportRecipeTest {

    @InjectMock
    ResourceParser parser;


    @Inject
    IJobService jobService;

    @InjectMock
    RecipeRepository recipeRepository;

    @InjectMock
    ImportJobRepository jobRepository;

    @Inject
    RecipeService subjectUnderTest;

    @Inject
    Event<JobReceivedEvent> received;

    @Inject
    ManagedExecutor executor;

    @Test
    @DisplayName("should import recipe from url")
    public void testImportRecipe() throws ExecutionException, InterruptedException, IOException {
        var event = new JobReceivedEvent();
        event.jobId = UUID.randomUUID();
        event.source = URI.create("http://meinerezepte.local/lasagne.html");

        var recipeId = UUID.randomUUID();

        var localParser = new ResourceHtmlParser();
        var data = localParser.readData(URI.create("lasagne.html"));
        Mockito.when(parser.readData(event.source)).thenReturn(data);
        Mockito.doAnswer(recipe -> {
            recipe.getArgument(0, Recipe.class).recipeId = recipeId;
            return null;
        }).when(recipeRepository).persist(any(Recipe.class));


        var future = received.fireAsync(event, NotificationOptions.ofExecutor(executor));
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
}
