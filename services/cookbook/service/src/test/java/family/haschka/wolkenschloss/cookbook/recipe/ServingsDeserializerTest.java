package family.haschka.wolkenschloss.cookbook.recipe;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import java.util.List;

@QuarkusTest
public class ServingsDeserializerTest {

    @Inject
    Jsonb jsonb;

    @Test
    public void shouldDeserializeServings() {

        var deserialized = jsonb.fromJson("{\"title\": \"TITLE\",  \"servings\": 4}", Recipe.class);
        var expected = new Recipe("unset", "TITLE", null, List.of(), new Servings(4), 0L);
        Assertions.assertEquals(expected, deserialized);
    }

    @Test
    public void shouldHaveDefaultValue() {
        var deserialized = jsonb.fromJson("{\"title\": \"TITLE\"}", Recipe.class);
        var expected = new Recipe("unset", "TITLE", null, List.of(), new Servings(1), 0L);

        Assertions.assertEquals(expected, deserialized);
    }

    @Test
    public void shouldExtractValue() {
        var deserialized = jsonb.fromJson("{\"title\": \"TITLE\", \"servings\": \"3 Portion(en)\"}", Recipe.class);
        var expected = new Recipe("unset", "TITLE", null, List.of(), new Servings(3), 0L);

        Assertions.assertEquals(expected, deserialized);
    }

    @Test
    public void shouldFailIfIsIsNull() {

        var recipe = jsonb.fromJson("{\"title\": \"TITLE\", \"servings\": \"3 Portion(en)\"}", Recipe.class);
        Assertions.assertEquals("unset", recipe.get_id());
    }

    @Test
    public void shouldSerialize() {
        var serialized = jsonb.toJson(new Servings(1));
        Assertions.assertEquals("1", serialized);
    }
}
