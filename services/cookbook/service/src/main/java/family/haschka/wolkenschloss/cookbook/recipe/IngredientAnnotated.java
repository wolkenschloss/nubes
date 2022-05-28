package family.haschka.wolkenschloss.cookbook.recipe;

import family.haschka.wolkenschloss.cookbook.recipe.Rational;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

public class IngredientAnnotated {

    @JsonbCreator
    public IngredientAnnotated(@JsonbProperty("quantity") Rational quantity, @JsonbProperty("unit") String unit, @JsonbProperty("name") String name) {
        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
    }

//    @JsonbProperty("quantity")
    public Rational quantity;

//    @JsonbProperty("unit")
    public String unit;

//    @JsonbProperty("name")
    public String name;
}
