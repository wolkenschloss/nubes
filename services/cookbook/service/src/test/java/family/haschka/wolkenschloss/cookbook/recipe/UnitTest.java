package family.haschka.wolkenschloss.cookbook.recipe;

import com.mongodb.assertions.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "cubic", "Liter", "decilitre", "Deziliter", "dl",
            "centilitre", "Centiliter", "cl",
            "millilitre", "Milliliter", "ml",
            "fluid ounce", "fl", "fl.oz.", "oz.fl.", "fl oz", "oz fl",
            "cup", "c", "Tasse", "Ta",
            "gram", "g", "Gramm",
            "decigram", "dg", "Dezigramm",
            "milligram", "mg", "Milligramm",
            "Dekagramm", "dag", "Deka",
            "kilogram", "kg", "Kilogramm",
            "pound", "lb", "Pfund",

            "bunch", "bu", "Bund", "Bd",
            "drop", "dr", "Tropfen", "Tr",
            "dash", "ds", "Spritzer", "Spr",
            "shot", "Schuss",
            "teaspoon", "ts", "tsp", "Teelöffel", "TL",
            "Barlöffel", "BL",
            "tablespoon", "tb", "tbsp", "Esslöffel", "EL",
            "Messerspitze", "Msp",
            "pinch", "pn", "Prise", "Pr",

            "can", "cn", "Dose", "Do",
            "carton", "ct", "Karton", "Kt",
            "piece", "pc", "Stück", "Stk",
            "package", "pk", "Packung", "Pk", "Päckchen", "Pck",
            "slice", "sl", "Scheibe", "Sch",
            "sheet", "sh", "Blatt", "Bl"
    })
    public void shouldCreateRegex(String testcase) {
        Pattern regex = Pattern.compile("^" + Unit.regex() + "$");
        Matcher matcher = regex.matcher(testcase);

        Assertions.assertTrue(matcher.matches());
    }

    enum UnitTestcase {
        CUBIC("cubic"),
        LITER("Liter"),
        DECILITER("decilitre", "Deziliter", "dl"),
        CENTILITER("centilitre", "Centiliter", "cl"),
        MILLILITER("millilitre", "Milliliter", "ml"),
        FLUID_ONCE("fluid ounce", "fl", "fl.oz.", "oz.fl."),
        CUP("cup", "c", "Tasse", "Ta");

        private final String unit;

        UnitTestcase(String unit, String... aliases) {
            this.unit = unit;
        }
    }
}
