package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.annotation.JsonbProperty;

public class IngredientAnnotations {
    @JsonbProperty(nillable = true)
    Rational quantity;

    @JsonbProperty(nillable = true)
    String unit;

    String name;
}
