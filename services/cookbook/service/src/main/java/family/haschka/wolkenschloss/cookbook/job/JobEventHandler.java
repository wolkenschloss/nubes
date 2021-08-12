package family.haschka.wolkenschloss.cookbook.job;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class JobEventHandler {
    @Inject
    JobService service;

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    Logger log;

    public void jobCompleted(/*@Observes*/ JobCompletedEvent event) {
        log.info("Job Event Handler job completed");
        service.jobCompleted(event);
    }
}
