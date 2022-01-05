package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/units/groups")
public class GroupsResource {

    @GET
    public Uni<Groups> list() {

        var groups = new Groups(Group.VOLUME, Group.WEIGHT, Group.KITCHEN, Group.COMMON);
        return Uni.createFrom().item(groups);
    }

    record Groups(Group... groups) {
    }
}
