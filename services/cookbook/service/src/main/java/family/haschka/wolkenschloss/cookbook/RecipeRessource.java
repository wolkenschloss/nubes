package family.haschka.wolkenschloss.cookbook;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/recipe")
public class RecipeRessource {

    @GET
    @Produces(APPLICATION_JSON)
    public String get() {
        return "hello";
    }
}
