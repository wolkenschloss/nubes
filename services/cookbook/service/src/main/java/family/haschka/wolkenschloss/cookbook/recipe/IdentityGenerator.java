package family.haschka.wolkenschloss.cookbook.recipe;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class IdentityGenerator {
    public UUID generate() {
        return UUID.randomUUID();
    }
}
