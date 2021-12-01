package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.recipe.TimeService;

import javax.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;

@ApplicationScoped
public class SystemTime implements TimeService {
    @Override
    public ZonedDateTime now() {
        return ZonedDateTime.now();
    }
}
