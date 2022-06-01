package family.haschka.wolkenschloss.cookbook.recipe.download;

import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import io.smallrye.mutiny.Uni;

import java.net.URI;
import java.util.function.Function;

public interface DataSource {
    Uni<Recipe> extract(URI source, Function<String, Recipe> transform);
}
