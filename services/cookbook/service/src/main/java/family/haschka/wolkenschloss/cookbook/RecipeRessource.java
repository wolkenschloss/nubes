package family.haschka.wolkenschloss.cookbook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/recipe")
public class RecipeRessource {

    private static final Logger logger = LoggerFactory.getLogger(RecipeRessource.class);
    @Inject
    RecipeService service;

    @GET
    @Produces(APPLICATION_JSON)
    public List<Recipe> get() {
        return service.list();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Response post(Recipe recipe, @Context UriInfo uriInfo) {
        service.save(recipe);

        var location = uriInfo.getAbsolutePathBuilder()
                .path(recipe.id.toString())
                .build();

        return Response
                .created(location)
                .entity(recipe)
                .build();
    }
}
