package family.haschka.wolkenschloss.cookbook.logging;


import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

/**
 * Dieser Test erzeugt keine Ausgabe, wenn er alleine ausgeführt wird. Das ist ein Fehler.
 * Bevor du jetzt alles umkonfigurierst, denke daran, dass ich drei Tage benötigt habe,
 * Um die Konfiguration herzustellen. Es kann nur schlechter werden. Wenn du keine Ahnung
 * von JbossLogger, Slf4j und JUL hast, lass die Finger weg!
 *
 * Wenn der Test zusammen mit den anderen Tests ausgeführt wird, sehen die Logausgaben
 * vernünftig aus.
 *
 * HINWEIS:
 *
 * Es besteht keine Notwendigkeit in Unittests zu loggen! Unittests prüfen erwartete
 * und aktuelle Werte und geben durch die Assertion ggf. eine Fehlermeldung über das
 * Unittest Framework aus, falls die Erwartung nicht erfüllt wird.
 *
 * Die mit @QuarkusTest gekennzeichneten Tests starten die Anwendung. Die Anwendung
 * muss ein Log Protokoll erstellen. Im Test wird das Log Protokoll auf der Konsole
 * ausgegeben. Damit kann beim Test das Log Protokoll überprüft werden. Manchmal ist
 * das bei der Entwicklung des Tests hilfreich.
 *
 */
@QuarkusTest
public class JbossLoggerTest {

    //  private static final Logger logger = Logger.getLogger(JbossLoggerTest.class);

    // Ein JBoss Logger kann per CDI direkt in den Test geimpft werden.
    @Inject
    public Logger logger;

    @Test
    public void infoTest() {
        logger.infov("JbossLogger Information");
    }

    @Test
    public void warnTest() {
        logger.warnv("JbossLogger {0}", "Warnung");
    }

    @Test
    public void errorTest() {
        logger.errorv("JBossLogger {0}", "Fehler");
    }
}
