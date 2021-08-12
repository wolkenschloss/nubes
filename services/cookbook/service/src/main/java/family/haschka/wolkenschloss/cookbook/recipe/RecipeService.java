package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.job.JobCompletedEvent;
import family.haschka.wolkenschloss.cookbook.job.JobReceivedEvent;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeService {

    @Inject
    RecipeRepository recipeRepository;

    @Inject
    Logger log;

    public void save(Recipe recipe) {

        recipeRepository.persist(recipe);
    }

    private PanacheQuery<Recipe> getQuery(String search) {
        if (search == null || search.equals("")) {
            return recipeRepository.findAll(Sort.by("title"));
        }

        return recipeRepository.find("title like ?1}", search, Sort.by("title"));
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

    @Inject
    Event<JobCompletedEvent> completed;

    public void steal(@ObservesAsync JobReceivedEvent event) {
        try {
            var thief = new RecipeImport();
            var recipes = thief.extract(event.source);

            if (recipes.size() == 0) {
                throw new RecipeParseException("The data source does not contain an importable recipe");
            }

            recipes.get(0).recipeId = UUID.randomUUID();
            recipeRepository.persist(recipes.get(0));

            var done = new JobCompletedEvent();
            done.error = Optional.empty();
            done.jobId = event.jobId;
            done.location = Optional.of(UriBuilder.fromUri("/recipe/{id}").build(recipes.get(0).recipeId));

            completed.fire(done);

        } catch (IOException e) {
            var done = new JobCompletedEvent();
            done.error = Optional.of("The data source cannot be read");
            done.jobId = event.jobId;
            done.location = Optional.empty();

            log.info("Can not steal recipe", e);

            completed.fire(done);

            log.warn("send completed event");
        } catch (RecipeParseException e) {
            var done = new JobCompletedEvent();
            done.error = Optional.of(e.getMessage());
            done.jobId = event.jobId;
            done.location = Optional.empty();

            log.info("Can not steal recipe", e);

            completed.fire(done);
        }
    }
}
