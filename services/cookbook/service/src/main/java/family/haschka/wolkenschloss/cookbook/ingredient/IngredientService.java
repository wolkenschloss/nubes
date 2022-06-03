package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IngredientService {

    public IngredientService(IngredientRepository repository, IdentityGenerator identityGenerator) {
        this.repository = repository;
        this.identityGenerator = identityGenerator;
    }

    private final IngredientRepository repository;
    private final IdentityGenerator identityGenerator;

    Logger log = Logger.getLogger(IngredientService.class);

    public Uni<Ingredient> create(String title) {
        log.infov("creating ingredient {0}", title);

        return repository.find("name like ?1", title)
                .firstResultOptional()
                .onItem().transformToUni(i -> i.map(j -> Uni.createFrom().item(j))
                        .orElseGet(() -> {
                            log.infov("no duplicates found. creating new ingredient {0}", title);
                    var ingredient = new Ingredient(identityGenerator.generate(), title);
                    return repository.persist(ingredient);
                }));
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

    @Incoming("ingredient-required")
    public Uni<Void> onIngredientRequired(Message<IngredientRequiredEvent> event) {
        return Uni.createFrom().item(event)
                .log("ingredient required")
                .chain(e -> create(e.getPayload().getIngredient()))
                .onItem().transformToUni(x -> Uni.createFrom().completionStage(event.ack()));
    }
}
