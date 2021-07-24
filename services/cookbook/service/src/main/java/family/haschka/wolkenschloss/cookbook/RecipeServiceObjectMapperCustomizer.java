package family.haschka.wolkenschloss.cookbook;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.jackson.ObjectMapperCustomizer;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RecipeServiceObjectMapperCustomizer implements ObjectMapperCustomizer {
    @Inject
    Logger logger;

    @Override
    public void customize(ObjectMapper objectMapper) {
        logger.info("Customizing ObjectMapper");
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }
}
