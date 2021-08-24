package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class RationalCodec implements Codec<Rational> {
    @Override
    public Rational decode(BsonReader reader, DecoderContext decoderContext) {
        var type = reader.getCurrentBsonType();
        if (type == BsonType.INT64) {
            return new Rational((int) reader.readInt64(), 1);
        }

        return Rational.parse(reader.readString());
    }

    @Override
    public void encode(BsonWriter writer, Rational value, EncoderContext encoderContext) {
        writer.writeString(value.toString());
    }

    @Override
    public Class<Rational> getEncoderClass() {
        return Rational.class;
    }
}
