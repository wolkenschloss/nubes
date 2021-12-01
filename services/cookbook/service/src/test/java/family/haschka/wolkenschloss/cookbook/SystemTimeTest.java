package family.haschka.wolkenschloss.cookbook;

import family.haschka.wolkenschloss.cookbook.recipe.TimeService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.time.ZonedDateTime;

@QuarkusTest
public class SystemTimeTest {

    @Inject
    TimeService time;

    @Test
    public void getNow() {
        var now = time.now();
        Assertions.assertTrue(ZonedDateTime.now().compareTo(now) >= 0);
    }
}
