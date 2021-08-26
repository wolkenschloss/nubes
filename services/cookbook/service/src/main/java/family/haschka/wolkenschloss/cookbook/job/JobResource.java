package family.haschka.wolkenschloss.cookbook.job;

import family.haschka.wolkenschloss.cookbook.recipe.IdentityGenerator;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
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

    @Inject
    IdentityGenerator identityGenerator;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> post(ImportJob job, @Context UriInfo uriInfo) {
        job.state = State.IN_PROGRESS;
        job.jobId = identityGenerator.generate();

        return service.addJob(job)
                .map(event -> uriInfo.getAbsolutePathBuilder()
                        .path(GET_PATH)
                        .build(event.jobId()))
                .map(location -> Response
                        .status(Response.Status.CREATED)
                        .header("Location", location.toString())
                        .entity(job)
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
