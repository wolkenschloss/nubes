package family.haschka.wolkenschloss.cookbook.logging;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Disabled
public class Slf4jLoggerTest {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jLoggerTest.class);

    @Test
    public void infoTest() {
        logger.info("Slf4jLogger {}", "Information");
    }

    @Test
    public void warnTest() {
        logger.warn("Slf4jLogger {}", "Warnung");
    }

    @Test
    public void errorTest() {
        logger.error("Slf4jLogger {}", "Fehler");
    }
}
