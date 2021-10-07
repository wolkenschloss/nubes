package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.ingredient.IngredientRequiredEvent;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import javax.enterprise.context.ApplicationScoped;

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
        recipe.recipeId = identityGenerator.generate();
        return recipeRepository.persist(recipe)
                .onItem().invoke(this::lookupIngredients);
    }

    private void lookupIngredients(Recipe recipe) {
        recipe.ingredients.forEach(ingredient ->
                emitter.send(new IngredientRequiredEvent(recipe.recipeId, ingredient.name)));
    }
}
