package family.haschka.wolkenschloss.cookbook.recipe;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeImport {
    private final HttpParser reader;

    public RecipeImport(HttpParser reader) {
        this.reader = reader;
    }

    public List<Recipe> extract(URI source) throws IOException {
        var config = new JsonbConfig()
                .withAdapters(new RecipeAdapter());

        var jsonb = JsonbBuilder.create(config);

        return reader.readData(source).stream()
                .filter(this::isRecipe)
                .map(s -> jsonb.fromJson(s, Recipe.class))
                .collect(Collectors.toList());
    }

    public boolean isRecipe(String s) {
        var reader = Json.createReader(new StringReader(s));
        var structure = reader.read();
        try {
            var value = structure.getValue("/@type");
            if (value.getValueType().equals(JsonValue.ValueType.STRING)) {
                var str = (JsonString) value;
                return str.getString().equals("Recipe");
            }
        } catch (JsonException exception) {
            return false;
        }
        return false;
    }
}
