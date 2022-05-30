package family.haschka.wolkenschloss.cookbook.ingredient;

import org.bson.*;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.UUID;

public class IngredientCodec implements CollectibleCodec<Ingredient> {

    private final Codec<Document> documentCodec;

    public IngredientCodec(CodecRegistry registry) {
        this.documentCodec = registry.get(Document.class);
    }

    @Override
    public Ingredient generateIdIfAbsentFromDocument(Ingredient document) {
        if (!documentHasId(document)) {
            return new Ingredient(UUID.randomUUID(), document.name());
        }

        return document;
    }

    @Override
    public boolean documentHasId(Ingredient document) {
        return document.id() != null;
    }

    @Override
    public BsonValue getDocumentId(Ingredient document) {
        if (!documentHasId(document)) {
            throw new IllegalStateException("document does not contain an id");
        }
        return new BsonBinary(document.id());
    }

    @Override
    public Ingredient decode(BsonReader reader, DecoderContext decoderContext) {
        Document document = documentCodec.decode(reader, decoderContext);
        return new Ingredient(
                document.get("_id", UUID.class),
                document.getString("name"));
    }

    @Override
    public void encode(BsonWriter writer, Ingredient value, EncoderContext encoderContext) {
        Document document = new Document();
        document.put("_id", value.id());
        document.put("name", value.name());

        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<Ingredient> getEncoderClass() {
        return Ingredient.class;
    }
}
