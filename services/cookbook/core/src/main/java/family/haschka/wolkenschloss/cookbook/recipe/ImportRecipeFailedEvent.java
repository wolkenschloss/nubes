package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.UUID;

public record ImportRecipeFailedEvent(UUID uuid, Throwable cause) {
}
