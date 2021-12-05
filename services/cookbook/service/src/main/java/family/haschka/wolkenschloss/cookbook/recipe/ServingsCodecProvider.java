package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class ServingsCodecProvider implements CodecProvider {
    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == Servings.class) {
            return (Codec<T>) new ServingsCodec();
        }

        return null;
    }
}
