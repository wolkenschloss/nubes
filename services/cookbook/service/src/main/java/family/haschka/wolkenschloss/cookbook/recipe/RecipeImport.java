package family.haschka.wolkenschloss.cookbook.recipe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeImport {

    public List<Recipe> extract(String content) {
        Stream<String> scripts = extractJsonLdScripts(content);

        var config = new JsonbConfig()
                .withDeserializers(new ServingsDeserializer(), new RationalDeserializer())
                .withAdapters(new RecipeAdapterFromRecipeAnnotated());

        var jsonb = JsonbBuilder.create(config);

        return scripts
                .filter(this::isRecipe)
                .map(s -> jsonb.fromJson(s, Recipe.class))
                .collect(Collectors.toList());
    }

    public Stream<String> extractJsonLdScripts(String content) {

        Document dom = Jsoup.parse(content);
        Elements scripts = dom.select("script[type=application/ld+json]");

        return scripts.stream()
                .map(Element::data);
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
