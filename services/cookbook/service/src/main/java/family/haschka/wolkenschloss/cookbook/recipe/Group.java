package family.haschka.wolkenschloss.cookbook.recipe;

public enum Group {

    VOLUME("Volumes", "", Unit.CUBIC),
    WEIGHT("Weights", "", Unit.GRAM),
    KITCHEN("Kitchen Terms", "", Unit.BUNCH),
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
