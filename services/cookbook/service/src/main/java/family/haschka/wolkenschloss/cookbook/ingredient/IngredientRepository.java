package family.haschka.wolkenschloss.cookbook.ingredient;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class IngredientRepository implements ReactivePanacheMongoRepositoryBase<Ingredient, UUID> {
}
