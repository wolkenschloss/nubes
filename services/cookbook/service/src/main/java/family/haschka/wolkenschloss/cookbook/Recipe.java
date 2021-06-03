package family.haschka.wolkenschloss.cookbook;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.quarkus.mongodb.panache.MongoEntity;
import io.quarkus.mongodb.panache.jackson.ObjectIdSerializer;
import org.bson.types.ObjectId;

public class Recipe {
    public ObjectId _id;
    public String title;
    public String herstellung;

    protected Recipe() {}

    public Recipe(String title, String herstellung) {

        this.title = title;
        this.herstellung = herstellung;
    }
}
