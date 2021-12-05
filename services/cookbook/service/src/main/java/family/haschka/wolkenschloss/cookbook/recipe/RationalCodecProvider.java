package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class RationalCodecProvider implements CodecProvider  {
    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == Rational.class) {
            return (Codec<T>) new RationalCodec();
        }

        return null;
    }
}
