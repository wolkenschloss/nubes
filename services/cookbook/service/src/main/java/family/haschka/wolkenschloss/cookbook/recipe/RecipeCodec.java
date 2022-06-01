package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecipeCodec implements CollectibleCodec<Recipe> {

    private final Codec<Servings> servingsCodec;
    private final Codec<Ingredient> ingredientCodec;

    public RecipeCodec(CodecRegistry registry) {
        this.servingsCodec = registry.get(Servings.class);
        this.ingredientCodec = registry.get(Ingredient.class);
    }

    @Override
    public Recipe generateIdIfAbsentFromDocument(Recipe document) {
        if(!documentHasId(document)) {
            return new Recipe(
                    ObjectId.get().toHexString(),
                    document.title(),
                    document.preparation(),
                    new ArrayList<>(document.ingredients()),
                    new Servings(document.servings().amount()),
                    0L);
        }

        return document;
    }

    @Override
    public boolean documentHasId(Recipe document) {
        return document._id() != null;
    }

    @Override
    public BsonValue getDocumentId(Recipe document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("document does not contain an id");
        }

        return new BsonObjectId(new ObjectId(document._id()));
    }

    @Override
    public Recipe decode(BsonReader reader, DecoderContext decoderContext) {

        reader.readStartDocument();
        ObjectId _id = null;
        String title = null;
        String preparation = null;
        List<Ingredient> ingredients = null;
        Servings servings = null;
        Long created = null;

        var bsonType = reader.readBsonType();
        while(bsonType != BsonType.END_OF_DOCUMENT) {
            var fieldName = reader.readName();
            switch (fieldName) {
                case "_id" -> _id = reader.readObjectId();
                case "title" -> title = reader.readString();
                case "preparation" -> {
                    if (bsonType == BsonType.STRING) {
                        preparation = reader.readString();
                    } else if (bsonType == BsonType.NULL) {
                        preparation = null;
                        reader.readNull();
                    } else {
                        throw new IllegalStateException("Unknown type of field 'preparation'");
                    }
                }
                case "ingredients" -> ingredients = readIngredients(reader, decoderContext);
                case "servings" -> servings = servingsCodec.decode(reader, decoderContext);
                case "created" -> created = reader.readInt64();
            }

            bsonType = reader.readBsonType();
        }

        reader.readEndDocument();

        return new Recipe(_id.toHexString(), title, preparation, ingredients, servings, created);
    }

    private List<Ingredient> readIngredients(BsonReader reader, DecoderContext decoderContext) {
        var list = new ArrayList<Ingredient>();
        reader.readStartArray();
        while(reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(ingredientCodec.decode(reader, decoderContext));
        }
        reader.readEndArray();

        return list;
    }

    @Override
    public void encode(BsonWriter writer, Recipe value, EncoderContext encoderContext) {

        writer.writeStartDocument();

        writer.writeObjectId("_id", new ObjectId(value._id()));
        writer.writeString("title", value.title());

        Optional.ofNullable(value.preparation()).ifPresentOrElse(
                preparation -> writer.writeString("preparation", preparation),
                () -> writer.writeNull("preparation"));

        writer.writeStartArray("ingredients");
        value.ingredients().forEach(ingredient -> ingredientCodec.encode(writer, ingredient, encoderContext));
        writer.writeEndArray();

        writer.writeName("servings");
        servingsCodec.encode(writer, value.servings(), encoderContext);
        writer.writeInt64("created", value.created());
        writer.writeEndDocument();
    }

    @Override
    public Class<Recipe> getEncoderClass() {
        return Recipe.class;
    }
}
