package family.haschka.wolkenschloss.cookbook.ingredient;

import java.util.UUID;

public record IngredientRequiredEvent(String recipeId, String ingredient) {
}
