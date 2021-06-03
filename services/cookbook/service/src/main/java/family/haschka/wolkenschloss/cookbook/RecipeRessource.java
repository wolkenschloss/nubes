package family.haschka.wolkenschloss.cookbook;


import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/recipe")
public class RecipeRessource {

    @Inject
    RecipeService service;

    @Inject
    Logger logger;

    @GET
    @Produces(APPLICATION_JSON)
    public List<Recipe> get() {
        return service.list();
    }

    @POST
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
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
    @Produces(APPLICATION_JSON)
    @Path("{id}")
    public Recipe get(@PathParam("id") UUID id) {
        return service.get(id).orElseThrow(NotFoundException::new);
    }

    @DELETE
    @Path("{id}")
    @Produces(APPLICATION_JSON)
    public Response delete(@PathParam("id") UUID id) {
        service.delete(id);
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    public Recipe put(@PathParam("id")String id, Recipe recipe) {
        service.update(recipe);
        return recipe;
    }

    @PATCH
    @Path("{id}")
    @Consumes("application/json-patch+json;charset=utf-8")
    @Produces(APPLICATION_JSON)
    public Recipe patch(@PathParam("id") String id, Recipe recipe) {
        service.update(recipe);
        return recipe;
    }
}
