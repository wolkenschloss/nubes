package family.haschka.wolkenschloss.cookbook;

import com.mongodb.client.MongoClient;
import io.quarkus.arc.profile.UnlessBuildProfile;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/mongodb")
@ApplicationScoped
public class MongoDbRessource {

    @Inject
    MongoClient mongoClient;

    private static Logger log = Logger.getLogger(MongoDbRessource.class);

    @DELETE
    @Path("{collection}")
    public void delete(@PathParam("collection") String collection) {
        log.infov("drop collection {0}", collection);
        mongoClient.getDatabase("cookbook").getCollection(collection).drop();
    }

    @DELETE
    public void delete() {
        mongoClient.getDatabase("cookbook").drop();
    }
}
