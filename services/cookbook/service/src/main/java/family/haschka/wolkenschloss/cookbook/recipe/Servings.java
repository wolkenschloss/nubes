package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

import static java.lang.String.valueOf;

public record Servings(@JsonbProperty("amount") int amount) {

    public static final int MIN = 1;
    public static final int MAX = 100;

    @JsonbCreator
    public Servings {
        if (amount < MIN || amount > MAX) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return valueOf(amount);
    }
}
