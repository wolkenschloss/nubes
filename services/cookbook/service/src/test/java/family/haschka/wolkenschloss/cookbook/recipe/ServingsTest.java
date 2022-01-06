package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ServingsTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 9, 10, 99, 100})
    public void shouldBeValid(int candidate) {
        var servings = new Servings(candidate);
        Assertions.assertEquals(servings, new Servings(candidate));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 101})
    public void shouldBeInvalid(int candidate) {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Servings(candidate));
    }
}
