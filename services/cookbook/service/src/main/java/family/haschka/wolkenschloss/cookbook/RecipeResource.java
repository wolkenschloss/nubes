package family.haschka.wolkenschloss.cookbook;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import org.jboss.logging.Logger;

@Path("/recipe")
public class RecipeResource {

    @Inject
    Logger logger;

    @Inject
    RecipeService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TableOfContents get(@QueryParam("from") int from, @QueryParam("to") int to) {
        logger.infov("GET /recipe?from={0}&to={1}", from, to);
        return service.list(from, to);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(Recipe recipe, @Context UriInfo uriInfo) {
        recipe.recipeId = UUID.randomUUID();
        service.save(recipe);

        var location = uriInfo.getAbsolutePathBuilder()
                .path(recipe.recipeId.toString())
                .build();

        return Response
                .created(location)
                .entity(recipe)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Recipe get(@PathParam("id") UUID id) {
        return service.get(id).orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Recipe put(@PathParam("id")String id, Recipe recipe) {
        service.update(recipe);
        return recipe;
    }

    @PATCH
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Recipe patch(@PathParam("id") String id, Recipe recipe) {
        service.update(recipe);
        return recipe;
    }
}
