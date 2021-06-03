package family.haschka.wolkenschloss.cookbook.logging;

import family.haschka.wolkenschloss.cookbook.LogService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
@QuarkusTestResource(value = LogLevelError.class, restrictToAnnotatedClass = true)
public class QuarkusLogging {

    @Inject
    LogService service;

    @Test
    public void logFromService() {
        service.log("Services sollen loggen.");
    }

    @Test
    public void logFromUnitTest() {
        var logger = Logger.getLogger(QuarkusLogging.class);
        logger.error("Unittests sollten nicht loggen!");
    }
}
