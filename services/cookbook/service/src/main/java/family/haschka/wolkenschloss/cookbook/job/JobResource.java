package family.haschka.wolkenschloss.cookbook.job;

import org.jboss.logging.Logger;

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

    @SuppressWarnings("CdiInjectionPointsInspection")
    @Inject
    Logger log;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(ImportJob job, @Context UriInfo uriInfo) {

        log.infov("POST /job");
        log.info(job);

        job.jobId = UUID.randomUUID();
        job.state = State.IN_PROGRESS;

        service.addJob(job);

        var location = uriInfo.getAbsolutePathBuilder()
                .path(GET_PATH)
                .build(job.jobId);

        log.info("post end");
        log.infov("Response Location Header: {0}", location.toString());

        return Response.status(Response.Status.CREATED)
                .header("Location", location.toString())
                .entity(job)
                .build();
    }

    @GET
    @Path(GET_PATH)
    @Produces(MediaType.APPLICATION_JSON)
    public ImportJob get(@PathParam("id")UUID id) {
        log.infov("GET /job/{0}", id);
        return service.get(id).orElseThrow(NotFoundException::new);
    }
}
