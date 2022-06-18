package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                    document.getTitle(),
                    document.getPreparation(),
                    new ArrayList<>(document.getIngredients()),
                    new Servings(document.getServings().getAmount()),
                    0L, null);
        }

        return document;
    }

    @Override
    public boolean documentHasId(Recipe document) {
        return !document.get_id().equals("unset");
    }

    @Override
    public BsonValue getDocumentId(Recipe document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("document does not contain an id");
        }

        return new BsonObjectId(new ObjectId(document.get_id()));
    }

    @Override
    public Recipe decode(BsonReader reader, DecoderContext decoderContext) {

        reader.readStartDocument();
        ObjectId _id = null;
        String title = null;
        String preparation = null;
        List<Ingredient> ingredients = null;
        Servings servings = null;
        long created = 0L;

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

        return new Recipe(
                Objects.requireNonNull(_id).toHexString(),
                Objects.requireNonNull(title),
                preparation,
                Objects.requireNonNull(ingredients),
                Objects.requireNonNull(servings),
                created,
                null);
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

        writer.writeObjectId("_id", new ObjectId(value.get_id()));
        writer.writeString("title", value.getTitle());

        Optional.ofNullable(value.getPreparation()).ifPresentOrElse(
                preparation -> writer.writeString("preparation", preparation),
                () -> writer.writeNull("preparation"));

        writer.writeStartArray("ingredients");
        value.getIngredients().forEach(ingredient -> ingredientCodec.encode(writer, ingredient, encoderContext));
        writer.writeEndArray();

        writer.writeName("servings");
        servingsCodec.encode(writer, value.getServings(), encoderContext);
        writer.writeInt64("created", value.getCreated());
        writer.writeEndDocument();
    }

    @Override
    public Class<Recipe> getEncoderClass() {
        return Recipe.class;
    }
}
