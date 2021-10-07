package family.haschka.wolkenschloss.cookbook.ingredient;

import java.util.ArrayList;
import java.util.UUID;

public record RecipeAddedEvent(UUID recipeId,
                               ArrayList<Ingredient> ingredients) {
}
