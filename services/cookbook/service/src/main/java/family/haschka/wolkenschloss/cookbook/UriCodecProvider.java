package family.haschka.wolkenschloss.cookbook;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

import java.net.URI;

public class UriCodecProvider implements CodecProvider {
    @Override
    @SuppressWarnings("unchecked")
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == URI.class) {
            return (Codec<T>) new UriCodec();
        }

        return null;
    }
}
