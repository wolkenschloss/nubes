package family.haschka.wolkenschloss.cookbook;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.net.URI;

public class UriCodec implements Codec<URI> {

    @Override
    public URI decode(BsonReader reader, DecoderContext decoderContext) {
        return URI.create(reader.readString());
    }

    @Override
    public void encode(BsonWriter writer, URI value, EncoderContext encoderContext) {
        writer.writeString(value.toString());
    }

    @Override
    public Class<URI> getEncoderClass() {
        return URI.class;
    }
}
