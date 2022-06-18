package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;
import org.bson.types.ObjectId;

import javax.json.bind.Jsonb;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum RecipeFixture {

    CHILI_CON_CARNE(getChiliConCarne(), "chili.html"),
    LASAGNE(getLasagne(), "lasagne.html"),
    ANTIPASTI(getAntipasti(), "antipasti.html");

    RecipeFixture(Recipe recipe, String resource) {
        this.recipe = recipe;
        this.resource = resource;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static final String LOREM = """
            Lorem ipsum dolor sit amet, consetetur sadipscing elitr,
            et dolore magna aliquyam erat, sed diam voluptua. At rebum.
                        
            Duis autem vel eum iriure dolor in hendrerit in vulputate
            Lorem ipsum dolor sit amet, consectetuer aliquam erat volutpat.
                        
            Ut wisi enim ad minim veniam, quis nostrud exerci tation
            qui blandit praesent luptatum zzril delenit augue facilisi.""";

    private final Recipe recipe;
    public final String resource;

    public URI getRecipeSource() throws URISyntaxException {
        var uri = this.getClass().getClassLoader().getResource(resource);
        return Objects.requireNonNull(uri).toURI();
    }

    public Recipe withId() {
        return withId(ObjectId.get().toHexString());
    }

    public Recipe withId(String id) {
        return new Recipe(
                id,
                this.recipe.getTitle(),
                this.recipe.getPreparation(),
                new ArrayList<>(recipe.getIngredients()),
                this.recipe.getServings(),
                0L, null);
    }

    private static Recipe getLasagne() {

        return new Recipe(
                "unset",
                "Lasagne",
                LOREM,
                List.of(
                        new Ingredient("Hackfleisch", new Rational(500), "g"),
                        new Ingredient("Zwiebel(n)", new Rational(1), null),
                        new Ingredient("Knoblauchzehen", new Rational(2), null),
                        new Ingredient("Petersilie oder TK", new Rational(1), "Bund"),
                        new Ingredient("Tomatenmark", new Rational(1), "EL"),
                        new Ingredient("Tomaten, geschälte (800g)", new Rational(1), "Dose"),
                        new Ingredient("Etwas Rotwein", null, null)),
                new Servings(1),
                0L, null);
    }

    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet";

    private static Recipe getAntipasti() {
        return new Recipe(
                "unset",
                "Antipasti",
                LOREM_IPSUM,
                List.of(
                        new Ingredient("Olivenöl", new Rational(500), "ml"),
                        new Ingredient("Knoblauchzehen", new Rational(4), null),
                        new Ingredient("getrocknete italienische Kräuter", new Rational(4), "EL")),
                new Servings(4),
                0L, null);
    }

    private static Recipe getChiliConCarne() {
        return new Recipe(
                "unset",
                "Chili con carne",
                "Preparation",
                List.of(
                        new Ingredient("Rinderhackfleisch", new Rational(800), "g"),
                        new Ingredient("Tomaten, geschält", new Rational(1200), "g"),
                        new Ingredient("Zimtstange", new Rational(1), null)
                ),
                new Servings(1),
                0L, null);
    }

    public String asJson(Jsonb jsonb) {
        return jsonb.toJson(this.recipe);
    }

    public Uni<Recipe> toUni() {
        return Uni.createFrom().item(this.recipe);
    }

    public String read() throws URISyntaxException, IOException {
        try (InputStream in = this.getRecipeSource().toURL().openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, Charset.defaultCharset());
        }
    }

    public Recipe get() {
        return withId("unset");
    }
}
