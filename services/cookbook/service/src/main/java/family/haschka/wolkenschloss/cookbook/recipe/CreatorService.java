package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.ingredient.IngredientRequiredEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Objects;


@ApplicationScoped
public class CreatorService {

    private  final ImageRepository imageRepository;
    private final RecipeRepository recipeRepository;
    private final IdentityGenerator identityGenerator;
    private final Emitter<IngredientRequiredEvent> emitter;
    private String rootPath;
    private final Vertx vertx;


    public CreatorService(
            @ConfigProperty(name = "quarkus.http.root-path") String rootPath,
            Vertx vertx,
            ImageRepository imageRepository,
            RecipeRepository recipeRepository,
            IdentityGenerator identityGenerator,
            @Channel("ingredient-required") Emitter<IngredientRequiredEvent> emitter) {
        this.rootPath = rootPath;


        this.vertx = vertx;
        this.imageRepository = imageRepository;
        this.recipeRepository = recipeRepository;
        this.identityGenerator = identityGenerator;
        this.emitter = emitter;
    }

    public Uni<Recipe> save(Recipe recipe) {
        var toSave = new Recipe(
                identityGenerator.generateObjectId(),
                recipe.getTitle(),
                recipe.getPreparation(),
                new ArrayList<>(recipe.getIngredients()),
                new Servings(recipe.getServings().getAmount()),
                recipe.getCreated(),
                null
        );
        return recipeRepository.persist(toSave)
                .log("persisted")
                .onItem().invoke(this::lookupIngredients);
    }

    public void lookupIngredients(Recipe recipe) {
        recipe.getIngredients().forEach(ingredient ->
                emitter.send(new IngredientRequiredEvent(recipe.get_id(), ingredient.getName())));
    }

    private final static Logger log = Logger.getLogger(CreatorService.class);



    private URI imageUri(Image2 i) {
        Objects.requireNonNull(i, "Image ist null");

        try {
            var builder = UriBuilder.fromPath(rootPath);
            return builder.path(ImageResource.class)
                    .path(ImageResource.class.getMethod("get", String.class)).build(Objects.requireNonNull( i.getId(), "Image id ist null"));
        } catch (NoSuchMethodException exception) {
            throw new RuntimeException(exception);
        }
    }

    public Uni<Recipe> save(Recipe recipe, FileUpload upload) throws IOException {
        log.infof("create service save(%s, %s)", recipe.getTitle(), upload.uploadedFile().getFileName());

        return vertx.fileSystem().readFile(upload.uploadedFile().toAbsolutePath().toString())
                .onItem().transform(buffer -> new Image2(identityGenerator.generateObjectId(), upload.contentType(), buffer.getBytes()))
                .onItem().transformToUni(i -> imageRepository.persist(i))
                .log("image persisted")
                .onItem().transform(this::imageUri)
                .log("image url created")
                .onItem().transform(recipe::withImage)
                .log("recipe with image reference created")
                .onItem().transformToUni(r -> recipeRepository.persist(r))
                .log("recipe persisted")
                .onItem().invoke(this::lookupIngredients);
    }
}
