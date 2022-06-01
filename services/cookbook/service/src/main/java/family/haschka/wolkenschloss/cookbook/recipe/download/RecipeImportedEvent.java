package family.haschka.wolkenschloss.cookbook.recipe.download;

import java.net.URI;
import java.util.UUID;

public record RecipeImportedEvent(UUID jobId, URI location)  {
}
