package family.haschka.wolkenschloss.cookbook.ingredient;

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
        var query = repository.find("name like ?1", Sort.by("name"), search);
        var elements = query.range(from, to).list();

        return Uni.combine().all()
                .unis(query.count(), elements)
                .combinedWith(TableOfContents::new);
    }
}
