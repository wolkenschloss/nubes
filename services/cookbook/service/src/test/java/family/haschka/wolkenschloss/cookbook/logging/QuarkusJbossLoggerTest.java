package family.haschka.wolkenschloss.cookbook.logging;


import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class QuarkusJbossLoggerTest {
    private static final Logger logger = Logger.getLogger(QuarkusJbossLoggerTest.class);

    @Test
    public void infoTest() {

        logger.infov("QuarkusJbossLogger Information");
    }

    @Test
    public void warnTest() {
        logger.warnv("QuarkusJbossLogger {0}", "Warnung");
    }

    @Test
    public void errorTest() {
        logger.errorv("QuarkusJBossLogger {0}", "Fehler");
    }
}
