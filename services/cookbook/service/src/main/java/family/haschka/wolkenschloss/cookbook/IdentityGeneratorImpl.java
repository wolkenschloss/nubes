package family.haschka.wolkenschloss.cookbook;



import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class IdentityGeneratorImpl implements
        family.haschka.wolkenschloss.cookbook.recipe.IdentityGenerator,
        family.haschka.wolkenschloss.cookbook.job.IdentityGenerator {
    public UUID generate() {
        return UUID.randomUUID();
    }
}
