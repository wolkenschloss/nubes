package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/units/groups")
public class GroupsResource {

    @GET
    public Uni<Groups> list() {
        var group = new Group("Volumes");
        var groups = new Groups(group, new Group("Weights"), new Group("Kitchen terms"), new Group("Common Terms"));
        return Uni.createFrom().item(groups);
    }

    record Groups(Group... groups) {
    }

    record Group(String name) {
    }
}
