package family.haschka.wolkenschloss.cookbook;

import io.quarkus.mongodb.panache.MongoEntity;
import org.bson.types.ObjectId;

@MongoEntity
public class Recipe {
    public ObjectId _id;
    public String title;
    public String herstellung;

    public Recipe() {}

    public Recipe(String title, String herstellung) {

        this.title = title;
        this.herstellung = herstellung;
    }
}
