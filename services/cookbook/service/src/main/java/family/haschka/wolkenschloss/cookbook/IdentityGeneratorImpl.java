package family.haschka.wolkenschloss.cookbook;

import org.bson.types.ObjectId;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class IdentityGeneratorImpl implements
        family.haschka.wolkenschloss.cookbook.recipe.IdentityGenerator,
        family.haschka.wolkenschloss.cookbook.job.IdentityGenerator,
        family.haschka.wolkenschloss.cookbook.ingredient.IdentityGenerator {
    public UUID generate() {
        return UUID.randomUUID();
    }

    public String generateObjectId() {
        return ObjectId.get().toHexString();
    }
}
