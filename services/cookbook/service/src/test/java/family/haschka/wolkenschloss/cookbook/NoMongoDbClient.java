package family.haschka.wolkenschloss.cookbook;

import com.mongodb.client.MongoClients;
import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Collections;
import java.util.Set;

public class NoMongoDbClient implements QuarkusTestProfile {

    public Set<Class<?>> getEnabledAlternatives() {
        return Collections.emptySet();
    }

}
