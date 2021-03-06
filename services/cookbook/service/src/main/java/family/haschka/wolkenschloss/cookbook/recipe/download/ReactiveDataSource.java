package family.haschka.wolkenschloss.cookbook.recipe.download;

import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.download.DataSource;
import family.haschka.wolkenschloss.cookbook.recipe.download.RecipeParseException;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

import javax.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.util.function.Function;

@ApplicationScoped
public class ReactiveDataSource implements DataSource {

    private final Vertx vertx;

    public ReactiveDataSource(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public Uni<Recipe> extract(URI source, Function<String, Recipe> transform) {
        var client = WebClient.create(this.vertx);
        var content = client

                .getAbs(source.toString())
                .timeout(2000)
                .send()
                .invoke(this::validate)
                .map(HttpResponse::bodyAsString);

        return content.map(transform);
    }

    private void validate(HttpResponse<Buffer> response) {
        if (response.statusCode() != 200) {
            throw new RecipeParseException("The data source cannot be read");
        }
    }
}
