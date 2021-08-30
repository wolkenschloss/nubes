package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.job.JobCompletedEvent;
import family.haschka.wolkenschloss.cookbook.job.JobReceivedEvent;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.MalformedURLException;
import java.net.URI;
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
    ManagedExecutor executor;

    @Inject
    IdentityGenerator identityGenerator;

    @Inject
    EventBus bus;

    @Inject
    DataGrabber grabber;

    @ConsumeEvent("job-received")
    public void onJobReceived(JobReceivedEvent event) throws MalformedURLException {
        log.infov("onJobReceivedEvent: {0}", event);
        Uni<String> data = grabber.grab(event.source().toURL());
        data.map(body -> new RecipeImport().extract(body))
                .invoke(recipes -> {
                    if (recipes.size() != 1) {
                        throw new RecipeParseException("The data source does not contain an importable recipe");
                    }
                })
                .map(recipes -> recipes.get(0))
                .invoke(recipe -> recipe.recipeId = identityGenerator.generate())
                .flatMap(recipe -> recipeRepository.persist(recipe))
                .map(recipe -> new JobCompletedEvent(event.jobId(), UriBuilder.fromUri("/recipe/{id}").build(recipe.recipeId), null))
                .subscribe()
                .with(
                        completed -> bus.publish("job.completed", completed),
                        error -> bus.publish("job.completed", new JobCompletedEvent(event.jobId(), null, error.getMessage())));
    }

}
