package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.job.EventBusAddress;
import family.haschka.wolkenschloss.cookbook.job.JobCreatedEvent;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecipeService {

    private final Logger log;
    private final RecipeRepository recipeRepository;
    private final IdentityGenerator identityGenerator;
    private final EventBus bus;
    private final DataSource dataSource2;

    public RecipeService(Logger log, RecipeRepository recipeRepository, IdentityGenerator identityGenerator,
            EventBus bus, DataSource dataSource2) {

        this.log = log;
        this.recipeRepository = recipeRepository;
        this.identityGenerator = identityGenerator;
        this.bus = bus;
        this.dataSource2 = dataSource2;
    }

    public Uni<Recipe> save(Recipe recipe) {
        recipe.recipeId = identityGenerator.generate();
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


    @ConsumeEvent(EventBusAddress.CREATED)
//    https://github.com/janmaterne/quarkus-cdi-interceptor/blob/master/src/main/java/de/materne/quarkus/HelloWorldInterceptor.java
    public void onJobCreated(JobCreatedEvent event) {
        log.infov("on job created: ({0}, {1})", event.jobId(), event.source());
        var service = new RecipeImport();
        var id = service.grab(dataSource2, event)
                .log("job created before save")
                .flatMap(this::save)
                .map(recipe -> UriBuilder.fromResource(RecipeResource.class).path("{id}").build(recipe.recipeId))
                .invoke(location -> bus.send(EventBusAddress.IMPORTED, new RecipeImportedEvent(event.jobId(), location)))
                .onFailure().invoke(failure -> bus.send(EventBusAddress.FAILED, new ImportRecipeFailedEvent(event.jobId(), failure)))
                .replaceWithVoid()
                .subscribe()
                .with(V -> log.infov("recipe imported."), failure -> log.warnv("Can not import recipe: {0}", failure));
    }

//    @ConsumeEvent(EventBusAddress.RECEIVED)
//    public void onJobReceived(JobReceivedEvent event) throws MalformedURLException {
//        log.infov("onJobReceivedEvent: {0}", event);
//        grabber.grab(event.source().toURL())
//                .map(body -> new RecipeImport().extract(body))
//                .invoke(recipes -> {
//                    if (recipes.size() != 1) {
//                        throw new RecipeParseException("The data source does not contain an importable recipe");
//                    }
//                })
//                .map(recipes -> recipes.get(0))
//                .flatMap(recipe -> save(recipe))
//                .map(recipe -> new JobCompletedEvent(event.jobId(), UriBuilder.fromUri("/recipe/{id}").build(recipe.recipeId), null))
//                .onFailure().recoverWithItem(throwable -> new JobCompletedEvent(event.jobId(),null, throwable.getMessage()))
//                .subscribe()
//                .with(
//                        completed -> bus.send(EventBusAddress.COMPLETED, completed),
//                        error -> bus.send(EventBusAddress.COMPLETED, new JobCompletedEvent(event.jobId(), null, error.getMessage())));
//    }
}
