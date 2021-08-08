package family.haschka.wolkenschloss.cookbook;

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

    @Inject
    IJobService service;

    @Inject
    Logger log;

    @POST
    public Response post(ImportJob job, @Context UriInfo uriInfo) {

        log.infov("POST /job");
        log.info(job);

        job.setJobId(UUID.randomUUID());
        job.setState(ImportJob.State.IN_PROGRESS);

         service.addJob(job);

        var location = uriInfo.getAbsolutePathBuilder()
                .path(job.getJobId().toString())
                .build();
        log.info("post end");
        return Response.created(location).entity(job).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ImportJob get(@PathParam("id")UUID id) {
        return service.get(id).orElseThrow(NotFoundException::new);
    }
}
