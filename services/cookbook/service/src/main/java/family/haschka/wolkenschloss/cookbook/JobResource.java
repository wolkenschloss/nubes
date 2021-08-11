package family.haschka.wolkenschloss.cookbook;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.UUID;
import java.util.concurrent.Callable;

@Path("job")
public class JobResource {

    public static final String GET_PATH = "{id}";

    @Inject
    IJobService service;

    @Inject
    Logger log;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(ImportJob job, @Context UriInfo uriInfo) {

        log.infov("POST /job");
        log.info(job);

        job.setJobId(UUID.randomUUID());
        job.setState(ImportJob.State.IN_PROGRESS);

         service.addJob(job);


        var location = uriInfo.getAbsolutePathBuilder()
                .path(GET_PATH)
                .build(job.getJobId());

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
        return service.get(id).orElseThrow(NotFoundException::new);
    }
}
