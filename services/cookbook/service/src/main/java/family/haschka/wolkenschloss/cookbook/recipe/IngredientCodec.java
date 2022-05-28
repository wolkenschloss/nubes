package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.Objects;

public class IngredientCodec implements Codec<Ingredient> {

    private final Codec<Document> documentCodec;
    private final Codec<Rational> rationalCodec;

    public IngredientCodec(CodecRegistry codecRegistry) {
        this.documentCodec = codecRegistry.get(Document.class);
        this.rationalCodec = codecRegistry.get(Rational.class);

        Objects.requireNonNull(this.rationalCodec);
    }

    @Override
    public Ingredient decode(BsonReader reader, DecoderContext decoderContext) {

        reader.readStartDocument();

        String name = null;
        String unit = null;
        Rational quantity = null;

        while(reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            var fieldName = reader.readName();
            switch (fieldName) {
                case "name" -> name = reader.readString();
                case "unit" -> unit = reader.readString();
                case "quantity" -> quantity = rationalCodec.decode(reader, decoderContext);
            }
        }

        reader.readEndDocument();
        return new Ingredient(quantity, unit, name);
    }

    @Override
    public void encode(BsonWriter writer, Ingredient value, EncoderContext encoderContext) {

        var document = new Document();
        document.put("name", value.name);
        document.put("unit", value.unit);
        document.put("quantity", value.quantity);

        documentCodec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<Ingredient> getEncoderClass() {
        return Ingredient.class;
    }
}
