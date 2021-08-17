package family.haschka.nubes.cookbook.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NutritionCalculatorTests {
    @Test
    @Disabled
    public void canCalculateResult() {
        Recipe recipe = new Recipe();
        NutritionCalculator calculator = new NutritionCalculator();
        Label label = calculator.analyse(recipe);

        Assertions.assertEquals(label.carbohydrate, 0.1);
        Assertions.assertTrue(false);
    }

    @Test
    public void notAFailingTest() {
        Assertions.assertTrue(true);
    }
}
