package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.ingredient.IngredientRequiredEvent;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import io.vertx.mutiny.core.Vertx;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.resteasy.reactive.common.util.CaseInsensitiveMap;
import org.jboss.resteasy.reactive.server.core.multipart.DefaultFileUpload;
import org.jboss.resteasy.reactive.server.core.multipart.FormData;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;

@DisplayName("Creator Service")
@QuarkusTest
public class CreatorServiceTest {

    private static final String rootPath = "/cookbook";

    @Inject
    Vertx vertx;

    RecipeRepository repository;
    IdentityGenerator generator;
    IngredientRequiredEventEmitter emitter;

    ImageRepository imageRepository;

    UriInfo uriInfo;

    @BeforeEach
    public void mockCollaborators() {
        uriInfo = Mockito.mock(UriInfo.class);
        imageRepository = Mockito.mock(ImageRepository.class);
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

        CreatorService subjectUnderTest = new CreatorService(rootPath, vertx, imageRepository,  repository, generator, emitter);

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

        CreatorService subjectUnderTest = new CreatorService(rootPath, vertx, imageRepository, repository, generator, emitter);

        subjectUnderTest.save(recipe)
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .assertCompleted()
                .assertItem(recipeWithId);

        Mockito.verify(repository, Mockito.times(1)).persist(recipeWithId);
        Mockito.verify(generator, Mockito.times(1)).generateObjectId();
        Mockito.verify(emitter, Mockito.times(recipe.getIngredients().size())).send(any(IngredientRequiredEvent.class));
    }

    @Test
    @DisplayName("'save' should persist recipe image")
    public void testShouldPersistImage() throws IOException {

        var resource = this.getClass().getClassLoader().getResource("food.jpg");
        var content =this.getClass().getClassLoader().getResourceAsStream("food.jpg").readAllBytes();

        var formData = new FormData(1);
        var path = Path.of(Objects.requireNonNull(resource).getPath());
        var headers = new CaseInsensitiveMap<String>();
        headers.add("Content-Type", "image/png");
        formData.add("image", path, "image.png", headers);
        var upload = new DefaultFileUpload("image", formData.getFirst("image"));


        var imageId = ObjectId.get();

        var image = new Image2(imageId.toHexString(), "image/jpg", content);
//        var imageWithId = new Image(imageId.toHexString(), "image/jpg", content);

        var recipe = RecipeFixture.LASAGNE.get();
        var recipeWithImageUri = recipe.withImage(URI.create("/cookbook/images/%s".formatted(imageId.toHexString())));
        var recipeWithId = RecipeFixture.LASAGNE.withId();

        Mockito.when(generator.generateObjectId())
                .thenReturn(imageId.toHexString());

        Mockito.when(uriInfo.getBaseUri())
                        .thenReturn(URI.create("http://localhost:1234/cookbook"));

        Mockito.when(imageRepository.persist(image))
            .thenReturn(Uni.createFrom().item(image));

        Mockito.when(repository.persist(recipeWithImageUri))
                .thenReturn(Uni.createFrom().item(recipeWithId));

        recipe.getIngredients().forEach(ingredient -> Mockito.when(
                        emitter.send(new IngredientRequiredEvent(recipeWithId.get_id(), ingredient.getName())))
                .thenReturn(CompletableFuture.allOf()));

        CreatorService subjectUnderTest = new CreatorService(rootPath, vertx, imageRepository, repository, generator, emitter);

        var tester = subjectUnderTest.save(recipe, upload)
                .invoke(r -> Assertions.assertEquals(r, recipeWithId))
                .subscribe()
                .withSubscriber(UniAssertSubscriber.create())
                .awaitItem(Duration.ofSeconds(5))
                ;

        tester.assertItem(recipeWithId).assertCompleted();

        Mockito.verify(imageRepository, Mockito.times(1)).persist(image);
        Mockito.verify(repository, Mockito.times(1)).persist(recipeWithImageUri);
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

