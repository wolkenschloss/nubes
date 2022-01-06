package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Ingredient {

    // Required by Bson
    @SuppressWarnings("unused")
    public Ingredient() {
    }

    @JsonbCreator
    public Ingredient(
            @JsonbProperty("quantity") Rational quantity,
            @JsonbProperty("unit") String unit,
            @JsonbProperty("name") String name) {

        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
    }

    public String name;
    public String unit;
    public Rational quantity;

    public static Ingredient parse(String string) {
        var units = Unit.regex();
        var regex = "^(?<quant>" + Rational.REGEX + ")?\\s?((?<unit>" + units + ")?\\s(?<name>.*?))?$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(string);
        if (m.find())
        {
            Rational quant = Optional.ofNullable(m.group("quant"))
                    .filter(r -> !r.isEmpty())
                    .map(r -> Rational.parse(r.trim()))
                    .orElse(null);

            String unit = m.group("unit");
            String name = m.group("name");

            return new Ingredient(quant, unit, name);
        } else {
            return new Ingredient(null, null, string);
        }
    }

    @Override
    public String toString() {
        return Stream.of(quantity, unit, name)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(" "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(name, that.name) && Objects.equals(unit, that.unit) && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, unit, quantity);
    }

    public Ingredient scale(Rational factor) {

        var scaledQuantity = Optional.ofNullable(quantity)
                .map(q -> q.multiply(factor))
                .orElse(null);

        return new Ingredient(scaledQuantity, unit, name);
    }
}
