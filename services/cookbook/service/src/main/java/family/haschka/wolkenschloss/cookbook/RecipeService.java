package family.haschka.wolkenschloss.cookbook;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeService {

    @Inject
    RecipeRepository recipeRepository;

    public void save(Recipe recipe) {

        recipeRepository.persist(recipe);
    }

    private PanacheQuery<Recipe> getQuery(String search) {
        if (search == null || search.equals("")) {
            return recipeRepository.findAll(Sort.by("title"));
        }

        return recipeRepository.find("title like ?1}",  search, Sort.by("title"));
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
