package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;

import javax.enterprise.context.ApplicationScoped;
import java.net.URL;

@ApplicationScoped
public class DataGrabber {

    private final Vertx vertx;

    public DataGrabber(Vertx vertx) {
        this.vertx = vertx;
    }

    public Uni<String> grab(URL url) {
        var client = WebClient.create(this.vertx);
        return client.getAbs(url.toString())
                .send()
                .log("on job received: after send")
                .invoke(this::validate)
                .map(HttpResponse::bodyAsString);
    }

    private void validate(HttpResponse<Buffer> response) {
        if (response.statusCode() != 200) {
            throw new RecipeParseException("The data source cannot be read");
        }
    }
}