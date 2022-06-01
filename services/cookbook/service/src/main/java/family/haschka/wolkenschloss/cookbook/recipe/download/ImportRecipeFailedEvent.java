package family.haschka.wolkenschloss.cookbook.recipe.download;

import java.util.UUID;

public record ImportRecipeFailedEvent(UUID uuid, Throwable cause) {
}
