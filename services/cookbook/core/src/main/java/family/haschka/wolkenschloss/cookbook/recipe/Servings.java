package family.haschka.wolkenschloss.cookbook.recipe;

import static java.lang.String.valueOf;

public record Servings(int amount) {

    public static final int MIN = 1;
    public static final int MAX = 100;

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
