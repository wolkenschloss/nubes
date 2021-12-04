package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.job.JobCreatedEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@DisplayName("If a job was created to import a recipe")
public class ImportServiceTest {

    private interface ImportRecipeFailedEventEmitter extends Emitter<ImportRecipeFailedEvent> {}
    private interface NackFunction extends Function<Throwable, CompletionStage<Void>> {}

    @BeforeEach
    public void mockCollaborators() {
        dataSource = Mockito.mock(DataSource.class);
        creator = Mockito.mock(CreatorService.class);
        emitter = Mockito.mock(ImportRecipeFailedEventEmitter.class);
        nack = Mockito.mock(NackFunction.class);
    }

    private DataSource dataSource;
    private CreatorService creator;
    private ImportRecipeFailedEventEmitter emitter;
    private NackFunction nack;

    static Stream<Testcase> createTestcases() {
        return Arrays.stream(RecipeFixture.values())
                .map(fixture -> new Testcase(fixture, ObjectId.get().toHexString(), UUID.randomUUID()));
    }

    record Testcase(RecipeFixture fixture, String recipeId, UUID jobId) {

        private URI expectedLocation() {
            return UriBuilder.fromUri("/recipe/{id}").build(recipeId);
        }

        private URI source() throws URISyntaxException {
            return fixture.getRecipeSource();
        }

        private Multi<Message<JobCreatedEvent>> messages(NackFunction nack) throws URISyntaxException {
            var event = new JobCreatedEvent(jobId(), source());
            var message = Message.of(event).withNack(nack);
            return Multi.createFrom().item(message);
        }

        private Recipe getWithId() {
            return fixture.withId(recipeId);
        }

        @Override
        public String toString() {
            return fixture.get().title;
        }
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("createTestcases")
    @DisplayName("the recipe should have been imported if no errors occurred.")
    public void testImportRecipe(Testcase testcase) throws URISyntaxException {

        Mockito.when(dataSource.extract(eq(testcase.source()), any()))
                .thenReturn(testcase.fixture.toUni());

        Mockito.when(creator.save(testcase.fixture.get()))
                .thenReturn(Uni.createFrom().item(testcase.getWithId()));

        ImportService service = new ImportService(creator, dataSource, emitter);

        service.onJobCreated(testcase.messages(nack))
                .map(Message::getPayload)
                .subscribe()
                .withSubscriber(AssertSubscriber.create(1))
                .awaitCompletion()
                .assertItems(new RecipeImportedEvent(testcase.jobId(), testcase.expectedLocation()));

        //noinspection ReactiveStreamsUnusedPublisher
        Mockito.verify(creator, Mockito.times(1))
                        .save(testcase.fixture.get());

        //noinspection ReactiveStreamsUnusedPublisher
        Mockito.verify(dataSource, Mockito.times(1))
                .extract(eq(testcase.source()), any());
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("createTestcases")
    @DisplayName("the import of the recipe should fail if the recipe could not be read from the data source.")
    public void failOnDataSourceError(Testcase testcase) throws URISyntaxException {

        var failure = new RuntimeException("There was an error");

        Mockito.when(dataSource.extract(any(URI.class), any()))
                .thenReturn(Uni.createFrom().failure(failure));

        var service = new ImportService(creator, dataSource, emitter);

        service.onJobCreated(testcase.messages(nack))
                .map(Message::getPayload)
                .subscribe()
                .withSubscriber(AssertSubscriber.create(1))
                .awaitCompletion();

        Mockito.verify(nack, Mockito.times(1))
                        .apply(failure);

        //noinspection ReactiveStreamsUnusedPublisher
        Mockito.verify(dataSource, Mockito.times(1))
                .extract(eq(testcase.source()), any());

        Mockito.verify(emitter, Mockito.times(1))
                        .send(new ImportRecipeFailedEvent(testcase.jobId(), failure));
    }

    @ParameterizedTest(name = "{index}: {0}")
    @MethodSource("createTestcases")
    @DisplayName("the import of the recipe should fail if the recipe cannot be saved in the cookbook.")
    public void failOnPersistenceError(Testcase testcase) throws URISyntaxException {

        var failure = new RuntimeException("Error");

        Mockito.when(dataSource.extract(any(URI.class), any()))
                .thenReturn(testcase.fixture.toUni());

        Mockito.when(creator.save(testcase.fixture.get()))
                .thenReturn(Uni.createFrom().failure(failure));

        ImportService service = new ImportService(creator, dataSource, emitter);

        service.onJobCreated(testcase.messages(nack))
                .map(Message::getPayload)
                .subscribe()
                .withSubscriber(AssertSubscriber.create(1))
                .awaitCompletion()
                .assertItems();

        Mockito.verify(nack, Mockito.times(1))
                        .apply(failure);

        //noinspection ReactiveStreamsUnusedPublisher
        Mockito.verify(creator, Mockito.times(1))
                .save(testcase.fixture.get());

        //noinspection ReactiveStreamsUnusedPublisher
        Mockito.verify(dataSource, Mockito.timeout(1000).times(1))
                .extract(eq(testcase.source()), any());

        Mockito.verify(emitter, Mockito.times(1))
                        .send(new ImportRecipeFailedEvent(testcase.jobId, failure));
    }

    @AfterEach
    public void verifyNoMoreInteractions() {
        Mockito.verifyNoMoreInteractions(creator);
        Mockito.verifyNoMoreInteractions(dataSource);
        Mockito.verifyNoMoreInteractions(emitter);
        Mockito.verifyNoMoreInteractions(nack);
    }
}
