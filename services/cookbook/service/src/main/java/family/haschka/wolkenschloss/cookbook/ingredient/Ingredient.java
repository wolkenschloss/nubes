package family.haschka.wolkenschloss.cookbook.ingredient;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.Objects;
import java.util.UUID;

public class Ingredient {
    @BsonId
    private final UUID id;
    private final String name;

    public Ingredient(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public Ingredient withId(UUID id) {
        return new Ingredient(id, name);
    }
}
