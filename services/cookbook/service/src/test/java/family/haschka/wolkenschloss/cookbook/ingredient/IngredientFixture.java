package family.haschka.wolkenschloss.cookbook.ingredient;

import java.util.UUID;

public enum IngredientFixture {
    FLOUR("flour"),
    SUGAR("sugar");

    public final String title;

    IngredientFixture(String title) {
        this.title = title;
    }

    public Ingredient withId(UUID id) {
        return new Ingredient(id, title);
    }
}
