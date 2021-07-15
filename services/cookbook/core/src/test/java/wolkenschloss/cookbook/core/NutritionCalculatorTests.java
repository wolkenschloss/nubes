package wolkenschloss.cookbook.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NutritionCalculatorTests {
    @Test
    public void canCalculateResult() {
        Recipe recipe = new Recipe();
        NutritionCalculator calculator = new NutritionCalculator();
        Label label = calculator.analyse(recipe);

        Assertions.assertEquals(label.carbohydrate, 0.0);
    }
}
