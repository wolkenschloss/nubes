package family.haschka.wolkenschloss.cookbook.job;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class ImportJobRepository implements ReactivePanacheMongoRepositoryBase<ImportJob, UUID> {
}
