package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.job.JobCompletedEvent;
import family.haschka.wolkenschloss.cookbook.job.JobReceivedEvent;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.NotificationOptions;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeService {

    @Inject
    RecipeRepository recipeRepository;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    Logger log;

    public Uni<Recipe> save(Recipe recipe) {
        return recipeRepository.persist(recipe);
    }

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

    @Inject
    Event<JobCompletedEvent> completed;

    @Inject
    ManagedExecutor executor;

    @Inject
    IdentityGenerator identityGenerator;

    public void steal(@ObservesAsync JobReceivedEvent event) {
        try {
            var thief = new RecipeImport();
            var recipes = thief.extract(event.source());

            if (recipes.size() == 0) {
                throw new RecipeParseException("The data source does not contain an importable recipe");
            }

            recipes.get(0).recipeId = identityGenerator.generate();
            recipeRepository.persist(recipes.get(0))
                    .map(recipe -> new JobCompletedEvent(
                            event.jobId(),
                            UriBuilder.fromUri("/recipe/{id}").build(recipes.get(0).recipeId),
                            null))
                    .flatMap(done -> Uni.createFrom().completionStage(completed.fireAsync(done, NotificationOptions.ofExecutor(executor))))
                    .log("steal")
                    .await().atMost(Duration.ofSeconds(4));
        } catch (IOException e) {
            var done = new JobCompletedEvent(event.jobId(), null, "The data source cannot be read");
            log.infov("Can not steal recipe from {0}", event.source(), e);

            completed.fireAsync(done, NotificationOptions.ofExecutor(executor));

            log.warn("send completed event");
        } catch (RecipeParseException e) {
            var done = new JobCompletedEvent(event.jobId(), null, e.getMessage());

            log.info("Can not steal recipe", e);

            completed.fireAsync(done, NotificationOptions.ofExecutor(executor));
        }
    }
}
