package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;

import java.net.URI;
import java.util.function.Function;

public interface DataSource {
    Uni<Recipe> extract(URI source, Function<String, Recipe> transform);
}
