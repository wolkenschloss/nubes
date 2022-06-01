package family.haschka.wolkenschloss.cookbook.job;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class ImportJobCodecProvider implements CodecProvider {

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {

        if (clazz.equals(ImportJob.class)) {

            return (Codec<T>) new ImportJobCodec(registry);
        }

        return null;
    }
}
