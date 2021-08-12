package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class RecipeRepository implements PanacheMongoRepositoryBase<Recipe, UUID> {
}
