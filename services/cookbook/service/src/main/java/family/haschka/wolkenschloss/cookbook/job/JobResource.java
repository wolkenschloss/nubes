package family.haschka.wolkenschloss.cookbook.job;

import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;

@Path("job")
public class JobResource {

    public static final String GET_PATH = "{id}";

    @Inject
    JobService service;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> post(ImportJob job, @Context UriInfo uriInfo) {

        return service.create(job.getOrder())
                .map(entity -> Response
                        .status(Response.Status.CREATED)
                        .header("Location", uriInfo.getAbsolutePathBuilder().path(GET_PATH).build(entity.getJobId()))
                        .entity(entity)
                        .build());
    }

    @GET
    @Path(GET_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<ImportJob> get(@PathParam("id") UUID id) {
        return service.get(id)
                .map(optional -> optional.orElseThrow(NotFoundException::new));
    }
}
