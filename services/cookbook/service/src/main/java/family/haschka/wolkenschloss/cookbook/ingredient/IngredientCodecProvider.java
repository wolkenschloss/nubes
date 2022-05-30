package family.haschka.wolkenschloss.cookbook.ingredient;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class IngredientCodecProvider implements CodecProvider {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {

        if (clazz.equals(Ingredient.class)) {

            return (Codec<T>) new IngredientCodec(registry);
        }

        return null;
    }
}