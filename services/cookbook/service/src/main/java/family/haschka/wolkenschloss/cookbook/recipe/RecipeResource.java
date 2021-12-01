package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;
import java.util.UUID;

@Path("/recipe")
public class RecipeResource {

    @Inject
    RecipeService service;


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<TableOfContents> get(
            @DefaultValue("0") @QueryParam("from") int from,
            @DefaultValue("-1") @QueryParam("to") int to,
            @QueryParam("q") String search) {
        return service.list(from, to, search);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> post(Recipe recipe, @Context UriInfo uriInfo) {
        return creator.save(recipe)
                .log("saved")
                .map(r -> {
                    var location = uriInfo.getAbsolutePathBuilder()
                            .path(r.recipeId.toString())
                            .build();
                    return Response.created(location).entity(r).build();
                });
    }

    @Inject
    CreatorService creator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Uni<Recipe> get(@PathParam("id") UUID id, @QueryParam("servings") Integer servings) {
        return service.get(id, Optional.ofNullable(servings).map(Servings::new))
                .map(or -> or.orElseThrow(NotFoundException::new));
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete(@PathParam("id") UUID id) {
        return service.delete(id)
                .map(success -> Response.noContent().build());
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Recipe> put(@PathParam("id") String id, Recipe recipe) {
        return service.update(recipe);
    }

    @PATCH
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Recipe> patch(@PathParam("id") String id, Recipe recipe) {
        return service.update(recipe);
    }
}
