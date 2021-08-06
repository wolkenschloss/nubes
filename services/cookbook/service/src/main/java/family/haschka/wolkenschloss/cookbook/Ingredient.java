package family.haschka.wolkenschloss.cookbook;

public class Ingredient {

    public Ingredient() {}

    public Ingredient(Long quantity, String unit, String name) {

        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
    }


    public String name;
    public String unit;

    public Long quantity;
}
