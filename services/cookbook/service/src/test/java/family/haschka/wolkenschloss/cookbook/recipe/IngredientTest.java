package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class IngredientTest {

    public enum ParseIngredientTestcase {
        EMPTY("", new Ingredient(null, null, "")),
        QUANTITY_G_NAME("250 g Mehl", new Ingredient(new Rational(250), "g", "Mehl")),
        QUANTITY_KG_NAME("1 kg Potatoes", new Ingredient(new Rational(1), "kg", "Potatoes")),
        QUANTITY_ML_NAME("125 ml Milk", new Ingredient(new Rational(125), "ml", "Milk")),
        QUANTITY_L_NAME("1 l Water", new Ingredient(new Rational(1), "l", "Water")),
        QUANTITY_CL_NAME("1 cl Rum", new Ingredient(new Rational(1), "cl", "Rum")),
        QUANTITY_NAME("42 Zutat ohne Einheit", new Ingredient(new Rational(42), null, "Zutat ohne Einheit")),
        NAME("Zutat ohne Menge und Einheit", new Ingredient(null, null, "Zutat ohne Menge und Einheit")),
        UNIT_NAME("g name of ingredient", new Ingredient(null, null, "g name of ingredient")),
        NO_SPACES_BETWEEN_QUANTITY_AND_UNIT("20g Moon Sugar", new Ingredient(new Rational(20), "g", "Moon Sugar"));

        private final String string;
        private final Ingredient ingredient;

        ParseIngredientTestcase(String string, Ingredient ingredient) {

            this.string = string;
            this.ingredient = ingredient;
        }
    }

    @ParameterizedTest
    @EnumSource(ParseIngredientTestcase.class)
    void parseIngredientTest(ParseIngredientTestcase testcase) {
        var ingredient = Ingredient.parse(testcase.string);
        Assertions.assertEquals(testcase.ingredient, ingredient);
    }
}
