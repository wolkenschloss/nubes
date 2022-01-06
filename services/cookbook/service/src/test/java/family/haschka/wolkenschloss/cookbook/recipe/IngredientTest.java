package family.haschka.wolkenschloss.cookbook.recipe;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class IngredientTest {

    public enum ParseIngredientTestcase {
        EMPTY("", new Ingredient(null, null, null)),
        QUANTITY_G_NAME("250 g Mehl", new Ingredient(new Rational(250), "g", "Mehl")),
        QUANTITY_KG_NAME("1 kg Potatoes", new Ingredient(new Rational(1), "kg", "Potatoes")),
        QUANTITY_ML_NAME("125 ml Milk", new Ingredient(new Rational(125), "ml", "Milk")),
        QUANTITY_L_NAME("1 l Water", new Ingredient(new Rational(1), "l", "Water")),
        QUANTITY_CL_NAME("1 cl Rum", new Ingredient(new Rational(1), "cl", "Rum")),
        QUANTITY_NAME1("42 Zutat ohne Einheit", new Ingredient(new Rational(42), null, "Zutat ohne Einheit")),
        QUANTITY_NAME2("42 1/2 Zutat ohne Einheit", new Ingredient(new Rational(85, 2), null, "Zutat ohne Einheit")),
        QUANTITY_NAME3("42½ Zutat ohne Einheit", new Ingredient(new Rational(85, 2), null, "Zutat ohne Einheit")),
        NAME("Zutat ohne Menge und Einheit", new Ingredient(null, null, "Zutat ohne Menge und Einheit")),
        UNIT_NAME("g name of ingredient", new Ingredient(null, "g", "name of ingredient")),
        UNIT_ONLY("g", new Ingredient(null, null, "g")),
        QUANTITY_ONLY("123", new Ingredient(new Rational(123), null, null)),
        QUANTITY_FRACTION("1/2", new Ingredient(new Rational(1,2), null, null)),
        QUANTITY_FRACTION1("1 1/2", new Ingredient(new Rational(3,2), null, null)),
        QUANTITY_FRACTION2("1 ½", new Ingredient(new Rational(3,2), null, null)),
        QUANTITY_FRACTION3("1½", new Ingredient(new Rational(3,2), null, null)),
        QUANTITY_FRACTION_UNIT_NAME1("1/2 cl Rum", new Ingredient(new Rational(1,2), "cl", "Rum")),
        QUANTITY_FRACTION_UNIT_NAME2("1/2 g Mehl", new Ingredient(new Rational(1,2), "g", "Mehl")),
        QUANTITY_FRACTION_UNIT_NAME3("1 1/2 kg Mehl", new Ingredient(new Rational(3,2), "kg", "Mehl")),
        QUANTITY_FRACTION_UNIT_NAME4("1 1/2kg Mehl", new Ingredient(new Rational(3,2), "kg", "Mehl")),
        QUANTITY_FRACTION_UNIT_NAME5("⅛ l Sahne", new Ingredient(new Rational(1,8), "l", "Sahne")),
        QUANTITY_FRACTION_UNIT_NAME6("1 ⅛ l Sahne", new Ingredient(new Rational(9,8), "l", "Sahne")),
        QUANTITY_FRACTION_UNIT_NAME7("1⅛ l Sahne", new Ingredient(new Rational(9,8), "l", "Sahne")),
        NO_SPACES_BETWEEN_QUANTITY_AND_UNIT1("20g Moon Sugar", new Ingredient(new Rational(20), "g", "Moon Sugar")),
        NO_SPACES_BETWEEN_QUANTITY_AND_UNIT2("2⅒g Moon Sugar", new Ingredient(new Rational(21, 10), "g", "Moon Sugar"));

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

    public enum ServingsTestcase {
        ONIONS(new Ingredient(new Rational(2), null, "Onions"),
                new Rational(5, 4),
                new Ingredient(new Rational(5, 2), null, "Onions")),

        // Das ist ein Beispiel, wo es nicht so gut ist mit Brüchen zu arbeiten.
        TOMATOES(new Ingredient(new Rational(800), "g", "Tomatoes"),
                new Rational(2, 3),
                new Ingredient(new Rational(1600, 3), "g", "Tomatoes"));

        private final Ingredient ingredient;
        private final Rational factor;
        private final Ingredient expected;

        ServingsTestcase(Ingredient ingredient, Rational factor, Ingredient expected) {
            this.ingredient = ingredient;
            this.factor = factor;
            this.expected = expected;
        }
    }

    @ParameterizedTest
    @EnumSource(ServingsTestcase.class)
    void scaleTests(ServingsTestcase testcase) {
        var actual = testcase.ingredient.scale(testcase.factor);
        Assertions.assertEquals(testcase.expected, actual);
    }

    public enum PrintIngredientTestcase {
        EMPTY(new Ingredient(null, null, null), ""),
        FULL(new Ingredient(new Rational(5, 2), "St.", "Zwiebeln"), "2 ½ St. Zwiebeln"),
        NAME(new Ingredient(null, null, "Rotwein"), "Rotwein"),
        QUANTITY(new Ingredient(new Rational(42), null, "Blaubeeren"), "42 Blaubeeren");

        final Ingredient ingredient;
        final String expectation;

        PrintIngredientTestcase(Ingredient ingredient, String expectation) {

            this.ingredient = ingredient;
            this.expectation = expectation;
        }
    }
    @ParameterizedTest
    @EnumSource
    public void shouldDisplayAsString(PrintIngredientTestcase testcase) {
        Assertions.assertEquals(testcase.expectation, testcase.ingredient.toString());
    }
}
