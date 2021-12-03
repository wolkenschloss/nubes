package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;
import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RecipeRepository implements ReactivePanacheMongoRepositoryBase<Recipe, ObjectId> {
}

