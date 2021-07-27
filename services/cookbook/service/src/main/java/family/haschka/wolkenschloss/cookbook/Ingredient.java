package family.haschka.wolkenschloss.cookbook;

import org.bson.codecs.pojo.annotations.BsonProperty;

import java.util.Optional;
import java.util.OptionalLong;

public class Ingredient {
    public String name;
    public String unit;

    public Long quantity;
}
