package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class IngredientService {
    @Inject
    IngredientRepository repository;

    public Uni<Ingredient> create(Ingredient ingredient) {
        return repository.persist(ingredient);
    }

    public Uni<TableOfContents> list(int from, int to, String search) {
        var query = createQuery(search);
        var elements = query.range(from, to).list();

        return Uni.combine().all()
                .unis(query.count(), elements)
                .combinedWith(TableOfContents::new);
    }

    private ReactivePanacheQuery<Ingredient> createQuery(String search) {
        if (search == null || search.equals("")) {
            return repository.findAll(Sort.by("name"));
        }

        return repository.find("name like ?1", Sort.by("name"), search);
    }

    @Inject
    EventBus bus;

    @Inject
    IdentityGenerator identityGenerator;

    @ConsumeEvent("recipe added")
    public void onRecipeAdded(RecipeAddedEvent event) {
        var ingredients = event.ingredients().stream()
                .map(i -> i.withId(identityGenerator.generate()))
                        .toList();

        repository.persist(ingredients)
                .subscribe()
                .with(
                        V -> ingredients.stream()
                                .map(i -> new IngredientAddedEvent(event.recipeId(), i))
                                .forEach(e -> bus.publish("ingredient added", e)),
                        failure -> System.err.println(failure.toString()));
    }
}
