package family.haschka.wolkenschloss.cookbook.ingredient;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/ingredient")
public class IngredientRessource {

    @GET
    public Uni<TableOfContents> get(
            @DefaultValue("0") @QueryParam("from") int from,
            @DefaultValue("-1") @QueryParam("to") int to,
            @QueryParam("q") String search) {
        return service.list(from, to, search);
    }

    @Inject
    IngredientService service;
}
