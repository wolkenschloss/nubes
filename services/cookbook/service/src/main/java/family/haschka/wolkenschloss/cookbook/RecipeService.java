package family.haschka.wolkenschloss.cookbook;

import com.mongodb.client.MongoClient;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public Optional<Recipe> get(UUID id) {
        var recipe = recipeRepository.findById(id);
        return Optional.ofNullable(recipe);
    }

    public void delete(UUID id) {
        recipeRepository.deleteById(id);
    }

    public void update(Recipe recipe) {
        recipeRepository.update(recipe);
    }
}
