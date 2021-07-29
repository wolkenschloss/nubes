package family.haschka.wolkenschloss.cookbook;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

@ApplicationScoped
public class RecipeService {

    @Inject
    Logger logger;

    @Inject
    RecipeRepository recipeRepository;

    public void save(Recipe recipe) {

        recipeRepository.persist(recipe);
    }

    public TableOfContents list(int from, int to) {
        var query = recipeRepository.findAll();
        var total = query.count();

        query.range(from, to);
        var range = query.list();

        var content = range.stream()
                .map(recipe -> new BriefDescription(recipe.recipeId, recipe.title))
                .collect(Collectors.toList());

        return new TableOfContents(total, content);
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
