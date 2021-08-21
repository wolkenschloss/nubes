package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class ServingsCodec implements Codec<Servings> {
    @Override
    public Servings decode(BsonReader reader, DecoderContext decoderContext) {
        return new Servings(reader.readInt32());
    }

    @Override
    public void encode(BsonWriter writer, Servings value, EncoderContext encoderContext) {
        writer.writeInt32(value.amount());
    }

    @Override
    public Class<Servings> getEncoderClass() {
        return Servings.class;
    }
}
