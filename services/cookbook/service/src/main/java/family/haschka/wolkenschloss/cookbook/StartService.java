package family.haschka.wolkenschloss.cookbook;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;


@ApplicationScoped
public class StartService {
    private static final Logger log = LoggerFactory.getLogger(StartService.class);

    @Inject
    Project project;

    void printVersion(@Observes StartupEvent event) {
        log.info("Starting {} {} {}", project.group(), project.name(), project.version());
        log.info("ref {} sha {}", project.ref(), project.sha());
    }
}
