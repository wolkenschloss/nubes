package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

public record Servings(@JsonbProperty("amount") int amount) {
    @JsonbCreator
    public Servings {
        if (amount < 1 || amount > 10) {
            throw new IllegalArgumentException();
        }
    }
}
