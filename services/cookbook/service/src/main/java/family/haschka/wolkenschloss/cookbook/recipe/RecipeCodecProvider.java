package family.haschka.wolkenschloss.cookbook.recipe;

import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class RecipeCodecProvider implements CodecProvider {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {

        if(clazz.equals(Recipe.class)) {
            return (Codec<T>) new RecipeCodec(registry);
        }

        return null;
    }
}
