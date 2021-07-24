package family.haschka.wolkenschloss.cookbook;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.UUID;

@Path("/recipe")
public class RecipeResource {

    @Inject
    RecipeService service;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Recipe> get() {
        return service.list();
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
