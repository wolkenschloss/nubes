package family.haschka.wolkenschloss.cookbook;

import java.util.UUID;

public class BriefDescription {
    public UUID recipeId;
    public String title;

    public BriefDescription(UUID recipeId, String title) {
        this.recipeId = recipeId;
        this.title = title;
    }
}
