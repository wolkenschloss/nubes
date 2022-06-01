package family.haschka.wolkenschloss.cookbook.recipe

import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

// https://www.kochwiki.org/wiki/Zubereitung:Ma%C3%9Fe_und_Gewichte
enum class Unit(val unit: String, vararg val aliases: String) {
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
    SHEET("sheet", "sh", "Blatt", "Bl");

//    @JvmField
//    val aliases: Array<String>
//
//    init {
//        this.aliases = aliases
//    }

    fun pattern(): String {
        return unit + "|" + java.lang.String.join("|", Arrays.asList(*aliases))
    }

    companion object {
        @JvmStatic
        fun regex(): String {
            return Arrays.stream(values()).map { obj: Unit -> obj.pattern() }.collect(Collectors.joining("|"))
        }

        @JvmStatic
        fun stream(): Stream<String> {
            return Arrays.stream(values()).flatMap { u: Unit -> Arrays.stream(u.aliases) }
        }

        fun list(): List<String> {
            return stream().collect(Collectors.toList())
        }
    }
}