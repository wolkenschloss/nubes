package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class RecipeCodec implements CollectibleCodec<Recipe> {

    private final Codec<Document> documentCodec;
    private final Codec<Servings> servingsCodec;
    private final Codec<Ingredient> ingredientCodec;

    public RecipeCodec(CodecRegistry registry) {
        this.documentCodec = new DocumentCodec(registry);
        this.servingsCodec = registry.get(Servings.class);
        this.ingredientCodec = registry.get(Ingredient.class);
    }

    @Override
    public Recipe generateIdIfAbsentFromDocument(Recipe document) {
        if(!documentHasId(document)) {
            return new Recipe(
                    ObjectId.get(),
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

        return new BsonObjectId(document._id());
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

        while(reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            var fieldName = reader.readName();
            switch (fieldName) {
                case "_id" -> _id = reader.readObjectId();
                case "title" -> title = reader.readString();
                case "preparation" -> preparation = reader.readString();
                case "ingredients" -> ingredients = readIngredients(reader, decoderContext);
                case "servings" -> servings = servingsCodec.decode(reader, decoderContext);
                case "created" -> created = reader.readInt64();
            }
        }

        reader.readEndDocument();

        return new Recipe(_id, title, preparation, ingredients, servings, created);
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
        Document document = new Document();
        document.put("_id", value._id());
        document.put("title", value.title());
        document.put("preparation", value.preparation());
        document.put("ingredients", value.ingredients());
        document.put("servings", value.servings());
        document.put("created", value.created());

        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<Recipe> getEncoderClass() {
        return Recipe.class;
    }
}
