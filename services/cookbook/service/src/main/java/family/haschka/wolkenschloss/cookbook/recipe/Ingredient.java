package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ingredient {

    public Ingredient() {}

    public Ingredient(Rational quantity, String unit, String name) {

        this.name = name;
        this.unit = unit;
        this.quantity = quantity;
    }

    public String name;
    public String unit;

    public Rational quantity;

    public static Ingredient parse(String string) {
        // TODO: rational statt long für quantity
        Pattern p = Pattern.compile("(?<quant>[1-9][0-9]*)\\s*(?<unit>g|kg|ml|cl|l)?\\s*(?<name>.*)");

        Matcher m = p.matcher(string);
        if (m.find())
        {
            String quant = m.group("quant");
            String unit = m.group("unit");
            String name = m.group("name");
            return new Ingredient(Rational.parse(quant), unit, name);
        } else {
            return new Ingredient(null, null, string);
        }

    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "name='" + name + '\'' +
                ", unit='" + unit + '\'' +
                ", quantity=" + quantity +
                '}';
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
}
