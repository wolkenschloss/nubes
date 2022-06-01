package family.haschka.wolkenschloss.cookbook.ingredient;

import java.util.UUID;

public record IngredientAddedEvent(UUID recipeId,
                                   Ingredient ingredient) {
}
