package family.haschka.wolkenschloss.cookbook;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

@ApplicationScoped
public class StartService {
    private static final Logger log = Logger.getLogger(StartService.class);

    @Inject
    Project project;

    @Inject
    VersionControlSystem vcs;

    void printVersion(@Observes StartupEvent event) {
        log.infov("Starting {0}:{1}:{2}", project.group(), project.name(), project.version());
        vcs.commit().ifPresent(commit -> log.infov("Version Control System - Commit: {0}", commit));
        vcs.ref().ifPresent(ref -> log.infov("Version Control System - Ref {0}", ref));
    }
}
