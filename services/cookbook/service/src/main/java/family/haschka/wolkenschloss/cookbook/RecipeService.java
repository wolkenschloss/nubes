package family.haschka.wolkenschloss.cookbook;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

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
}
