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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeImport {

    public List<Recipe> extract(URI source) throws IOException {

        String content = download(source);
        Stream<String> scripts = extractJsonLdScripts(content);

        var config = new JsonbConfig()
                .withAdapters(new RecipeAdapter());

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

    private String download(URI source) throws IOException {
        try (InputStream in = source.toURL().openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, Charset.defaultCharset());
        }
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
