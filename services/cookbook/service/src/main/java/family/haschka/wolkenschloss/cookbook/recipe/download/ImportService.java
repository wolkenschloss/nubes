package family.haschka.wolkenschloss.cookbook.recipe.download;

import family.haschka.wolkenschloss.cookbook.job.JobCreatedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.CreatorService;
import family.haschka.wolkenschloss.cookbook.recipe.ImportRecipeFailedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeImportedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeResource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.reactive.messaging.*;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

@ApplicationScoped
public class ImportService {

    private final CreatorService creator;
    private final DataSource dataSource;
    private final Emitter<ImportRecipeFailedEvent> emitter;

    public ImportService(
            CreatorService creator,
            DataSource dataSource,
            @Channel("import-failed") Emitter<ImportRecipeFailedEvent> emitter) {

        this.creator = creator;
        this.dataSource = dataSource;
        this.emitter = emitter;
    }

    @Incoming("job-created")
    @Outgoing("recipe-imported")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public Multi<Message<RecipeImportedEvent>> onJobCreated(Multi<Message<JobCreatedEvent>> event) {
        var service = new RecipeImport();
        return event.onItem().transformToUniAndMerge(e ->
                service.grab(dataSource, e.getPayload())
                        .chain(creator::save)
                        .map(recipe -> UriBuilder.fromResource(RecipeResource.class).path("{id}").build(recipe.get_id()))
                        .map(location -> new RecipeImportedEvent(e.getPayload().getJobId(), location))
                        .call(x -> Uni.createFrom().completionStage(e.ack()))
                        .map(e::withPayload)
                        .onFailure().invoke(failure -> {
                            emitter.send(new ImportRecipeFailedEvent(e.getPayload().getJobId(), failure));
                            e.nack(failure);
                        })
                        .onFailure().recoverWithNull());
    }
}
