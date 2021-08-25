package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class RecipeRepository implements ReactivePanacheMongoRepositoryBase<Recipe, UUID> {
}

