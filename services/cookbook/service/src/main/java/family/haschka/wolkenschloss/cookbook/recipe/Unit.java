package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// https://www.kochwiki.org/wiki/Zubereitung:Ma%C3%9Fe_und_Gewichte
public enum Unit {
    // Volumenangaben
    CUBIC("cubic", "Kubikzentimeter", "cc", "centimeter", "ccm"),
    LITER("Liter", "l"),
    DECILITER("decilitre", "Deziliter", "dl"),
    CENTILITER("centilitre", "Centiliter", "cl"),
    MILLILITER("millilitre", "Milliliter", "ml"),
    FLUID_ONCE("fluid ounce", "fl", "fl[\\. ]oz\\.?", "oz[\\. ]fl\\.?"),
    CUP("cup", "c", "Tasse", "Ta"),

    // Gewichtsangaben
    GRAM("gram", "g", "Gramm"),
    DECIGRAM("decigram", "dg", "Dezigramm"),
    MILLIGRAM("milligram", "mg", "Milligramm"),
    DEKAGRAM("Dekagramm", "dag", "Deka"),
    KILOGRAM("kilogram", "kg", "Kilogramm"),
    POUND("pound", "lb", "Pfund"),

    // Küchenbegriffe
    BUNCH("bunch", "bu", "Bund", "Bd"),
    DROP("drop", "dr", "Tropfen", "Tr"),
    DASH("dash", "ds", "Spritzer", "Spr"),
    SHOT("shot", "Schuss"),
    TEASPOON("teaspoon", "ts", "tsp", "Teelöffel", "TL", "Barlöffel", "BL"),
    TABLESPOON("tablespoon", "tb", "tbsp", "Esslöffel", "EL"),
    MESSERSPITZE("Messerspitze", "Msp"),
    PINCH("pinch", "pn", "Prise", "Pr"),

    // Allgemeine Begriffe. Es fehlen noch Modifikationen klein, mittelgroß, groß
    CAN("can", "cn", "Dose", "Do"),
    CARTON("carton", "ct", "Karton", "Kt"),
    PIECE("piece", "pc", "Stück", "Stk"),
    PACKAGE("package", "pk", "Packung", "Pk", "Päckchen", "Pck"),
    SLICE("slice", "sl", "Scheibe", "Sch"),
    SHEET("sheet", "sh", "Blatt", "Bl")
    ;


    final String unit;
    final String[] aliases;

    Unit(String unit, String... aliases) {
        this.unit = unit;
        this.aliases = aliases;
    }

    public String pattern() {
        return unit  + "|" + String.join("|", Arrays.asList(aliases));
    }

    public static String regex() {
        return Arrays.stream(Unit.values()).map(Unit::pattern)
                .collect(Collectors.joining("|"));
    }

    static public Stream<String> stream() {
        return Arrays.stream(Unit.values()).flatMap(u -> Arrays.stream(u.aliases));
    }

    static public List<String> list() {
        return stream().collect(Collectors.toList());
    }
}
