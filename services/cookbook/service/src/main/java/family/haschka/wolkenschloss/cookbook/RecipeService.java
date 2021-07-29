package family.haschka.wolkenschloss.cookbook;

import io.quarkus.mongodb.panache.PanacheQuery;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeService {

    @Inject
    Logger logger;

    @Inject
    RecipeRepository recipeRepository;

    public void save(Recipe recipe) {

        recipeRepository.persist(recipe);
    }

    private PanacheQuery<Recipe> getQuery(String search) {
        if (search == null || search.equals("")) {
            logger.infov("Empty search criteria");
            return recipeRepository.findAll();
        }

        logger.infov("searching for >{0}<", search);
        return recipeRepository.find("title like ?1}",  search);

        // Das ist eine genaue Suche, und die funktioniert
        // return recipeRepository.find("{'title': ?1}",  search);
    }

    public TableOfContents list(int from, int to, String search) {

        var query = getQuery(search);
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
