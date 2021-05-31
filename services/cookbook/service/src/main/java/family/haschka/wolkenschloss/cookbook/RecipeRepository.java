package family.haschka.wolkenschloss.cookbook;

import io.quarkus.mongodb.panache.PanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecipeRepository implements PanacheMongoRepository<Recipe> {
}
