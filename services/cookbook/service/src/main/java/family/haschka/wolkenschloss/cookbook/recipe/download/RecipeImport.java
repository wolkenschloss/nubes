package family.haschka.wolkenschloss.cookbook.recipe.download;

import family.haschka.wolkenschloss.cookbook.job.JobCompletedEvent;
import family.haschka.wolkenschloss.cookbook.job.JobCreatedEvent;
import family.haschka.wolkenschloss.cookbook.recipe.RationalDeserializer;
import family.haschka.wolkenschloss.cookbook.recipe.Recipe;
import family.haschka.wolkenschloss.cookbook.recipe.ServingsDeserializer;
import family.haschka.wolkenschloss.cookbook.recipe.SingletonCollector;
import io.smallrye.mutiny.Uni;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipeImport {

    JsonbConfig config = new JsonbConfig()
            .withDeserializers(new ServingsDeserializer(), new RationalDeserializer())
            .withAdapters(new RecipeAdapterFromRecipeAnnotated());

    Jsonb jsonb = JsonbBuilder.create(config);

    public Uni<Recipe> grab(DataSource dataSource, JobCreatedEvent event) {

        return dataSource.extract(event.source(), content -> extractJsonLdScripts(content).filter(this::isRecipe)
                        .map(script -> jsonb.fromJson(script, Recipe.class))
                        .collect(SingletonCollector.toItem()))
                .onFailure(SingletonCollector.TooFewItemsException.class).transform(failure -> new RuntimeException(
                        "The data source does not contain an importable recipe"))
                .onFailure(SingletonCollector.TooManyItemsException.class).transform(failure -> new RuntimeException(
                        "Data source contains more than one recipe"));
    }

    public List<Recipe> extract(String content) throws Exception {
        Stream<String> scripts = extractJsonLdScripts(content);

        var config = new JsonbConfig()
                .withDeserializers(new ServingsDeserializer(), new RationalDeserializer())
                .withAdapters(new RecipeAdapterFromRecipeAnnotated());

        try (Jsonb jsonb = JsonbBuilder.create(config)) {
            return scripts
                    .filter(this::isRecipe)
                    .map(s -> jsonb.fromJson(s, Recipe.class))
                    .collect(Collectors.toList());
        }
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

    public record GrabResult(Recipe entity, JobCompletedEvent event) {
    }
}
