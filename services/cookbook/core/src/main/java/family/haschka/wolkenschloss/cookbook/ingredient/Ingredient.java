package family.haschka.wolkenschloss.cookbook.ingredient;

import java.util.UUID;

public record Ingredient(UUID id, String name) {

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
