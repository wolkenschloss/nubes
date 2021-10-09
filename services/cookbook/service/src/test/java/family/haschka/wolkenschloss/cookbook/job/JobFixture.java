package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.ImportRecipeFailedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeFixture;
import family.haschka.wolkenschloss.cookbook.recipe.RecipeImportedEvent;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public enum JobFixture {
    LASAGNE(RecipeFixture.LASAGNE),
    CHILI(RecipeFixture.CHILI_CON_CARNE),
    ANTIPASTI(RecipeFixture.ANTIPASTI);

    final RecipeFixture source;

    JobFixture(RecipeFixture source) {

        this.source = source;
    }

    public Testcase testcase() {
        return new Testcase(this, UUID.randomUUID(), UUID.randomUUID());
    }

    public static record Testcase(JobFixture fixture, UUID jobId, UUID recipeId) {
        public ImportJob job() throws URISyntaxException {
            return ImportJob.create(jobId, fixture.source.getRecipeSource());
        }

        public JobCreatedEvent created() throws URISyntaxException {
            return new JobCreatedEvent(jobId, fixture.source.getRecipeSource());
        }

        public URI order() throws URISyntaxException {
            return fixture.source.getRecipeSource();
        }

        public URI location() {
            return UriBuilder.fromUri("/recipe/{id}").build(recipeId);
        }

        public RecipeImportedEvent imported() {
            return new RecipeImportedEvent(jobId, location());
        }

        public ImportRecipeFailedEvent failed(Throwable failure) {
            return new ImportRecipeFailedEvent(jobId, failure);
        }
    }
}
