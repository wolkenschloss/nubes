package family.haschka.wolkenschloss.cookbook;

import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/recipe")
public class RecipeRessource {

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
                .path(recipe._id.toString())
                .build();

        return Response
                .created(location)
                .entity(recipe)
                .build();
    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("{id}")
    public Recipe get(@PathParam("id") ObjectId id) {
        return service.get(id).orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Response delete(@PathParam("id") ObjectId id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Recipe put(@PathParam("id")ObjectId id, Recipe recipe) {
        return service.update(recipe);
    }
}
