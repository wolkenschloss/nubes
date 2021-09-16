package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.ImportRecipeFailedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeImportedEvent;
import io.quarkus.vertx.ConsumeEvent;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.eventbus.EventBus;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

// Ablauf der Recipe Import Saga:
// (1) Job erzeugen und persistieren. An den Aufrufer wird ein Ein-Element
// Strom, der gegebenenfalls Fehler enthält, zurückgegeben.
//   State: CREATED
//   Kompensation: keine.
// (2) Sendet das Ereignis JobCreatedEvent mit Request-Response Pattern. Der
// Response enthält die Location (URI) eines neu angelegten Recipes.
// (2a) Aktualisiert den Zustand des Jobs.
//  State: INCOMPLETE
//  Kompensation: State: ERROR


// 1. Job erzeugen (persistent). Es erfolgt keine Kompensation, falls dies
//  fehlschlägt. Stattdessen gibt der Endpunkt einen Fehler an den Client
//  zurück. Im Erfolgsfall wird das Ereignis JobCreatedEvent
//  (war JobReceivedEvent) wird veröffentlicht.
//  Job State: CREATED
// 2. onJobCreated (war onImportRecipe) liest die Datenquelle und gibt das
//  geparste Rezept als Antwort an den Absender des Ereignisses zurück. Das
//  Ereignis RecipeParsed wird veröffentlicht.
//  Job State: PARSED, job.recipe: response.recipe
//  Kompensation: Saga mit Fehler terminieren.
// 3. onRecipeParsed empfängt das Ereignis RecipeParsed und legt eine neue
//  Recipe Resource an. Erwarteter Statuscode ist 202 Accepted, aber jeder
//  2xx wäre ok.
// Job State: ACCEPTED. job.location = Location
// Kompensation: Saga mit Fehler terminieren
// 4. Die Saga wartet auf das Ereignis RecipeCreatedEvent. Das Ereignis wird
// vom Recipe Service gesendet, wenn das Recipe erfolgreich angelegt wurde.
// Job State: IMPORTED.
//
// Termination:
// Job State: ABORTED. job.error: Cause

//  Kompensation: Die Saga wird mit einem Fehler beendet.
// 4. Für jede Zutat in requestedIngredients des Jobs (parallel):
//  1. Das Ereignis IngredientRequestedEvent wird veröffentlicht.
//  2. Der Eintrag im job wird von job.requestedIngredient nach job.progress verschoben
//  3. Änderung des States des Jobs von PARSED in INGREDIENT_REQUESTED geändert
//  4. Die Zustandsänderung des Jobs wird gespeichert.
// 5. Für jedes IngredientRequestedEvent wird ein Ingredient Objekt angelegt und persistiert
//  (idempotent). Das Ingredient Objekt wird durch das Request Response Pattern an den
//  Aufrufer zurückgegeben.
// 6. Der Aufrufer entfernt aus job.progress die angeforderte Zutat und fügt die Zutat aus der
//  Antwort von 5 dem job.recipe hinzu.
//  Der Job wird persistiert.
//  Kompensation: Der Job wird mit einem Fehler terminiert.

// 5. Die Zutaten werden dem Rezept hinzugefügt. Das geänderte Rezept wird persistiert.
//  Kompensation: Das Rezept wird gelöscht. Die Saga wird mit einem Fehler beendet.
// 6. Die Saga ist beendet.

// Der Zugriff auf Recipe und Ingredient Collection erfolgt über HTTP.
// Zugriff auf Job erfolgt über das Repository.

@ApplicationScoped
public class JobService {

    JobService(IdentityGenerator identityGenerator, ImportJobRepository repository, EventBus eventBus, Logger log) {
        this.identityGenerator = identityGenerator;
        this.repository = repository;
        this.eventBus = eventBus;
        this.log = log;
    }

    private final ImportJobRepository repository;
    private final EventBus eventBus;
    private final Logger log;
    private final IdentityGenerator identityGenerator;

    @ConsumeEvent(EventBusAddress.IMPORTED)
    public void onRecipeImported(RecipeImportedEvent event) {
        log.infov("on recipe imported: ({0}, {1})", event.jobId(), event.location());
        repository.findById(event.jobId())
                .map(job -> job.located(event.location()))
                .flatMap(repository::update)
                .subscribe()
                .with(update -> log.infov("Job {0} updated to {1}", update.jobId, update.state),
                        failure -> log.warnv("Can not update job."));
    }

    @ConsumeEvent(EventBusAddress.FAILED)
    public void onImportRecipeFailed(ImportRecipeFailedEvent event) {
        log.infov("on import recipe failed: {0}", event.uuid());
        repository.findById(event.uuid())
                .map(job -> job.failed(event.cause()))
                .flatMap(repository::update)
                .subscribe()
                .with(update -> log.infov("job {0} updated to {1}", update.jobId, update.state),
                failure -> log.warnv("can not update job"));
    }

    // TODO: Wäre es nicht besser, ImportJob statt JobReceivedEvent zurückzugeben?
    public Uni<ImportJob> create(URI order) {
        var job = ImportJob.create(identityGenerator.generate(), order);
//        eventBus.publisher(EventBusAddress.CREATED)
        return repository.persist(job)
                .log()
                .ifNoItem().after(Duration.ofMillis(1000)).fail()
                .log()
                .invoke(created -> eventBus.send(EventBusAddress.CREATED, new JobCreatedEvent(created.jobId, created.order)))
//                .onItemOrFailure().invoke((item, failure) -> { log.infov("on item or failure: {0}, {1}", item, failure);})
                .log();
    }

    public Uni<Optional<ImportJob>> get(UUID id) {
        return repository.findByIdOptional(id);
    }

    @ConsumeEvent(EventBusAddress.COMPLETED)
    public void onCompleted(JobCompletedEvent event) {
        repository.findByIdOptional(event.jobId())
                .map(optional -> optional.orElseThrow(NotFoundException::new))
                .map(job -> job.complete(event.location(), event.error()))
                .flatMap(repository::update)
                .subscribe()
                .with(
                        job -> log.infov("Job '{0}' completed", job.jobId),
                        failure -> log.warnv("Failure: {0}", failure));
    }
}
