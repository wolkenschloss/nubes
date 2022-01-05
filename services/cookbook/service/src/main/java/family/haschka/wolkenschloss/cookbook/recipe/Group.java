package family.haschka.wolkenschloss.cookbook.recipe;

public enum Group {

    VOLUME("Volumes", "", Unit.CUBIC, Unit.LITER, Unit.DECILITER, Unit.CENTILITER, Unit.MILLILITER, Unit.FLUID_ONCE, Unit.CUP),
    WEIGHT("Weights", "", Unit.GRAM, Unit.DECIGRAM, Unit.MILLIGRAM, Unit.DEKAGRAM, Unit.KILOGRAM, Unit.POUND),
    KITCHEN("Kitchen Terms", "", Unit.BUNCH, Unit.DROP, Unit.DASH, Unit.SHOT, Unit.TEASPOON, Unit.TABLESPOON, Unit.MESSERSPITZE, Unit.PINCH),
    COMMON("Common Terms", "", Unit.CAN);

    final String description;
    final String name;
    final Unit[] units;

    Group(String name, String description, Unit...units) {
        this.name = name;
        this.description = description;
        this.units = units;
    }
}
