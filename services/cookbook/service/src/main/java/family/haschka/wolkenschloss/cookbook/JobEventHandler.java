package family.haschka.wolkenschloss.cookbook;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class JobEventHandler {
    @Inject
    IJobService service;

    @Inject
    Logger log;

    public void jobCompleted(@Observes JobCompletedEvent event) {
        log.info("Job Event Handler job completed");
        service.jobCompleted(event);
    }
}
