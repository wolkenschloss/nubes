package family.haschka.wolkenschloss.cookbook;

import io.quarkus.mongodb.panache.MongoEntity;
import org.bson.codecs.pojo.annotations.BsonId;

import java.util.UUID;

@MongoEntity
public class Recipe {
    @BsonId
    public UUID recipeId;

    public String title;
    public String herstellung;

    protected Recipe() {}

    public Recipe(String title, String herstellung) {

        this.title = title;
        this.herstellung = herstellung;
    }
}
