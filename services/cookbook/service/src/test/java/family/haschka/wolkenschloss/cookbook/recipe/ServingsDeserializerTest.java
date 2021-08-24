package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.json.bind.Jsonb;

@QuarkusTest
public class ServingsDeserializerTest {

    @Inject
    Jsonb jsonb;

    @Test
    public void shouldDeserializeServings() {

        var deserialized = jsonb.fromJson("{\"servings\": 4}", Recipe.class);
        var expected = new Recipe();
        expected.servings = new Servings(4);
        Assertions.assertEquals(expected, deserialized);
    }

    @Test
    public void shouldHaveDefaultValue() {
        var deserialized = jsonb.fromJson("{}", Recipe.class);

        var expected = new Recipe();
        expected.servings = new Servings(1);
        Assertions.assertEquals(expected, deserialized);
    }

    @Test
    public void shouldSerialize() {
        var serialized = jsonb.toJson(new Servings(1));
        Assertions.assertEquals("1", serialized);
    }
}
