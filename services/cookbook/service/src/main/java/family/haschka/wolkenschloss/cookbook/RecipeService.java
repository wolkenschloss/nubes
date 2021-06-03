package family.haschka.wolkenschloss.cookbook;

import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RecipeService {

    @Inject
    RecipeRepository recipeRepository;

    public void save(Recipe recipe) {
        recipeRepository.persist(recipe);
    }

    public List<Recipe> list() {
        return recipeRepository.listAll();
    }

    public Optional<Recipe> get(ObjectId id) {
        var recipe = recipeRepository.findById(id);
        return Optional.ofNullable(recipe);
    }

    public void delete(ObjectId id) {
        recipeRepository.deleteById(id);
    }

    public void update(Recipe recipe) {
        recipeRepository.update(recipe);
    }
}
