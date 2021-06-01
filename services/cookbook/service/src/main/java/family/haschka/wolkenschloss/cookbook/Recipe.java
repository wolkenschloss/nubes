package family.haschka.wolkenschloss.cookbook;

import io.quarkus.mongodb.panache.MongoEntity;
import org.bson.types.ObjectId;

@MongoEntity
public class Recipe {
    public ObjectId id;
    public String title;
    public String herstellung;

    Recipe() {}

    public Recipe(String title, String herstellung) {

        this.title = title;
        this.herstellung = herstellung;
    }
}
