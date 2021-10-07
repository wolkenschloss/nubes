package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeService {

    public RecipeService(Logger log, RecipeRepository recipeRepository) {
        this.log = log;
        this.recipeRepository = recipeRepository;
    }

    private final Logger log;
    private final RecipeRepository recipeRepository;

    private ReactivePanacheQuery<Recipe> getQuery(String search) {
        if (search == null || search.equals("")) {
            return recipeRepository.findAll(Sort.by("title"));
        }

        return recipeRepository.find("title like ?1}", search, Sort.by("title"));
    }

    public Uni<TableOfContents> list(int from, int to, String search) {

        var query = getQuery(search);
        var total = query.count();

        var page = query.range(from, to);
        var elements = page.list();

        // TODO: Statt alle Rezepte vollständig zu laden, könnte auch eine
        //  Projektion angewendet werden.
        var summaries = elements.map(e -> e.stream()
                .map(recipe -> new Summary(recipe.recipeId, recipe.title))
                .collect(Collectors.toList()));

        return Uni.combine().all().unis(summaries, total).asTuple()
                .map(t -> new TableOfContents(t.getItem2(), t.getItem1()));
    }

    public Uni<Optional<Recipe>> get(UUID id, Optional<Servings> servings) {
        return recipeRepository.findByIdOptional(id)
                .map(uni -> uni.map(recipe -> servings.map(recipe::scale).orElse(recipe)));
    }

    public Uni<Boolean> delete(UUID id) {
        return recipeRepository.deleteById(id);
    }

    public Uni<Recipe> update(Recipe recipe) {
        log.infov("update Recipe: {0}", recipe.toString());
        return recipeRepository.update(recipe);
    }
}
