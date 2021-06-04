package family.haschka.wolkenschloss.cookbook;

import io.quarkus.mongodb.panache.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.UUID;

@MongoEntity
public class Recipe {
    @BsonId
    public UUID recipeId;

    public String title;
    public String preparation;

    protected Recipe() {}

    public Recipe(String title, String preparation) {

        this.title = title;
        this.preparation = preparation;
    }
}
