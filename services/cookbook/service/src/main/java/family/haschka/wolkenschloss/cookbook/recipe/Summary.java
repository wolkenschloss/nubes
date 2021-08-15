package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.UUID;

public class Summary {
    public UUID recipeId;
    public String title;

    public Summary(UUID recipeId, String title) {
        this.recipeId = recipeId;
        this.title = title;
    }
}
