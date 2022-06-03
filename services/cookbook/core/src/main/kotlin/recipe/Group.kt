package family.haschka.wolkenschloss.cookbook.recipe

enum class Group(val title: String, val description: String, vararg val units: Unit) {
    VOLUME(
        "Volumes",
        "",
        Unit.CUBIC,
        Unit.LITER,
        Unit.DECILITER,
        Unit.CENTILITER,
        Unit.MILLILITER,
        Unit.FLUID_ONCE,
        Unit.CUP
    ),
    WEIGHT(
        "Weights",
        "",
        Unit.GRAM,
        Unit.DECIGRAM,
        Unit.MILLIGRAM,
        Unit.DEKAGRAM,
        Unit.KILOGRAM,
        Unit.POUND
    ),
    KITCHEN(
        "Kitchen Terms",
        "",
        Unit.BUNCH,
        Unit.DROP,
        Unit.DASH,
        Unit.SHOT,
        Unit.TEASPOON,
        Unit.TABLESPOON,
        Unit.MESSERSPITZE,
        Unit.PINCH
    ),
    COMMON(
        "Common Terms",
        "",
        Unit.CAN,
        Unit.CARTON,
        Unit.PIECE,
        Unit.PACKAGE,
        Unit.SLICE,
        Unit.SHEET);
}
