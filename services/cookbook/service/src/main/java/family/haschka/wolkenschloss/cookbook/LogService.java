package family.haschka.wolkenschloss.cookbook;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LogService {

    private static final Logger logger = Logger.getLogger(LogService.class);

    public void log(String message) {
        logger.warnf("Testausgabe: %s", message);
    }
}
