package family.haschka.wolkenschloss.cookbook;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

@Path("/recipe")
public class RecipeResource {

    @Inject
    Logger logger;

    @Inject
    RecipeService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TableOfContents get(
            @DefaultValue("0") @QueryParam("from") int from,
            @DefaultValue("-1") @QueryParam("to") int to,
            @QueryParam("q") String search) {
        logger.infov("GET /recipe?from={0}&to={1}&q={2}", from, to, search);
        return service.list(from, to, search);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(Recipe recipe, @Context UriInfo uriInfo) {
        logger.info("POST /recipe");
        logger.infov("recipe: {0}", recipe);
        recipe.recipeId = UUID.randomUUID();
        service.save(recipe);
        logger.info("recipe persisted");

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
