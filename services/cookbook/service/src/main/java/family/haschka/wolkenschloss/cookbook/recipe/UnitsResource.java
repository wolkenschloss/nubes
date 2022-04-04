package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Multi;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;

@Path("/units")
public class UnitsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Multi<Unit> list() {
        return Multi.createFrom().items(Arrays.stream(Unit.values()));
    }
}
