package family.haschka.wolkenschloss.cookbook.ingredient;

import java.util.UUID;

public enum IngredientFixture {
    FLOUR("flour");

    private final String name;

    IngredientFixture(String name) {

        this.name = name;
    }

    public Ingredient withId(UUID id) {
        return new Ingredient(id, name);
    }
}
