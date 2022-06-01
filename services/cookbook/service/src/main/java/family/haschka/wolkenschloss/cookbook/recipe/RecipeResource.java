package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Optional;

@Path("/recipe")
public class RecipeResource {
    public static final String TEXT_MARKDOWN = "text/markdown";

    @Inject
    RecipeService service;

    @Inject
    CreatorService creator;

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
                            .path(r._id().toString())
                            .build();
                    return Response.created(location).entity(r).build();
                });
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Uni<Recipe> get(@PathParam("id") String id, @QueryParam("servings") Integer servings) {
        return service.get(id, Optional.ofNullable(servings).map(Servings::new))
                .map(or -> or.orElseThrow(NotFoundException::new));
    }

    @GET
    @Produces(TEXT_MARKDOWN)
    @Path("{id}")
    public Uni<String> getMarkdown(@PathParam("id") String id) {
        return service.get(id, Optional.empty())
                .map(or -> or.orElseThrow(NotFoundException::new))
                .onItem().transformToUni(i -> Templates.recipe()
                        .data("recipe", i)
                        .createUni());
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance recipe();
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> delete(@PathParam("id") String id) {
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
