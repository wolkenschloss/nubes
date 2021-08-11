package family.haschka.wolkenschloss.cookbook.job;

import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class ImportJobRepository implements PanacheMongoRepositoryBase<ImportJob, UUID> {
}