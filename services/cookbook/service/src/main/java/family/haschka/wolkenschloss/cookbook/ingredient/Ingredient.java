package family.haschka.wolkenschloss.cookbook.ingredient;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.UUID;

public class Ingredient {
    @BsonId
    private final UUID id;
    private final String name;

    public Ingredient(UUID id, String name) {
        this.id = id;
        this.name = name;
    }
}
