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
import java.util.Optional;

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
                Optional.ofNullable(id).map(ObjectId::new).orElse(null),
                this.recipe.title(),
                this.recipe.preparation(),
                new ArrayList<>(recipe.ingredients()),
                this.recipe.servings(),
                0L);
    }

    private static Recipe getLasagne() {

        return new Recipe(
                null,
                "Lasagne",
                LOREM,
                List.of(
                        new Ingredient(new Rational(500), "g", "Hackfleisch"),
                        new Ingredient(new Rational(1), null, "Zwiebel(n)"),
                        new Ingredient(new Rational(2), null, "Knoblauchzehen"),
                        new Ingredient(new Rational(1), "Bund", "Petersilie oder TK"),
                        new Ingredient(new Rational(1), "EL", "Tomatenmark"),
                        new Ingredient(new Rational(1), "Dose", "Tomaten, geschälte (800g)"),
                        new Ingredient(null, null, "Etwas Rotwein")),
                new Servings(1),
                0L);
    }

    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet";

    private static Recipe getAntipasti() {
        return new Recipe(
                null,
                "Antipasti",
                LOREM_IPSUM,
                List.of(
                        new Ingredient(new Rational(500), "ml", "Olivenöl"),
                        new Ingredient(new Rational(4), null, "Knoblauchzehen"),
                        new Ingredient(new Rational(4), "EL", "getrocknete italienische Kräuter")),
                new Servings(4),
                0L);
    }

    private static Recipe getChiliConCarne() {
        return new Recipe(
                null,
                "Chili con carne",
                "Preparation",
                List.of(
                        new Ingredient(new Rational(800), "g", "Rinderhackfleisch"),
                        new Ingredient(new Rational(1200), "g", "Tomaten, geschält"),
                        new Ingredient(new Rational(1), null, "Zimtstange")
                ),
                new Servings(1),
                0L);
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
        return withId(null);
    }
}
