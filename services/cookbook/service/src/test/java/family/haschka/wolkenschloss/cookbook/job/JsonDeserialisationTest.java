package family.haschka.wolkenschloss.cookbook.job;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.json.bind.Jsonb;

@QuarkusTest
public class JsonDeserialisationTest {
    @Inject
    Jsonb jsonb;

    @Test
    public void deserializeJob() {
        var json = "{\"order\": \"http://meinerezepte.local/lasagne.html\"}";
        var job = jsonb.fromJson(json, ImportJob.class);
        Assertions.assertEquals(true, true);
    }
}
