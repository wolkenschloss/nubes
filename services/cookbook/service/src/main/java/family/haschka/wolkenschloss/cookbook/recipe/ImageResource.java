package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.io.ByteArrayInputStream;

@Path("images")
public class ImageResource {

    @Inject
    ImageRepository repository;

    @GET
    @Produces("image/jpeg")
    @Path("{id}")
    public Uni<Response> get(@PathParam("id") String id) {
        return repository.findById(id)
                .log("found")
                .onItem().transform(image -> Response.ok(
                        new ByteArrayInputStream(image.getByteArray()),
                                image.getContentType())
                        .build())
                .log("transform")
                ;
//        return Uni.createFrom().item(Response.ok().build());
    }

    private static final Logger log = Logger.getLogger(ImageResource.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("uri")
    public Uni<Response> get(@Context UriInfo uriInfo) throws NoSuchMethodException {
        log.info("images / uri called");
        var base = uriInfo.getBaseUri();
        var builder = UriBuilder.fromPath(base.getPath());
        return Uni.createFrom().item(
                Response.ok(
                        builder.path(ImageResource.class)
                        .path(ImageResource.class.getMethod("get", String.class)).build("123").toString(),
                        MediaType.TEXT_PLAIN
                ).build()
        );


//                .path() path(this::get)
    }
}
