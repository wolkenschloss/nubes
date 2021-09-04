package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

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
}
