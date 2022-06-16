package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.ingredient.IngredientRequiredEvent;
import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;


@ApplicationScoped
public class CreatorService {

    private final RecipeRepository recipeRepository;
    private final IdentityGenerator identityGenerator;
    private final Emitter<IngredientRequiredEvent> emitter;

    public CreatorService(
            RecipeRepository recipeRepository,
            IdentityGenerator identityGenerator,
            @Channel("ingredient-required") Emitter<IngredientRequiredEvent> emitter) {

        this.recipeRepository = recipeRepository;
        this.identityGenerator = identityGenerator;
        this.emitter = emitter;
    }

    public Uni<Recipe> save(Recipe recipe) {
        var toSave = new Recipe(
                new ObjectId(identityGenerator.generateObjectId()).toHexString(),
                recipe.getTitle(),
                recipe.getPreparation(),
                new ArrayList<>(recipe.getIngredients()),
                new Servings(recipe.getServings().getAmount()),
                recipe.getCreated()
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

    public Uni<Recipe> save(Recipe recipe, FileUpload upload) {
        log.infof("create service save(%s, %s)", recipe.getTitle(), upload.uploadedFile().getFileName());
        return recipeRepository.persist(recipe)
                .log("save file upload")
                .onItem().invoke(this::lookupIngredients);
    }
}
