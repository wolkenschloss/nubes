package family.haschka.wolkenschloss.cookbook.recipe;

import io.smallrye.mutiny.Uni;

import javax.json.bind.Jsonb;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public enum RecipeFixture {

    CHILI_CON_CARNE(getChiliConCarne(), "chili.html"),
    LASAGNE(getLasagne(), "lasagne.html"),
    ANTIPASTI(getAntipasti(), "antipasti.html");

    RecipeFixture(Recipe recipe, String resource) {
        this.recipe = recipe;
        this.resource = resource;
    }

    public final Recipe recipe;
    public final String resource;

    public URI getRecipeSource() throws URISyntaxException {
        var uri = this.getClass().getClassLoader().getResource(resource);
        return uri.toURI();
    }

    public Recipe withId(UUID id) {
        var recipeWithId = new Recipe(this.recipe.title, this.recipe.preparation);
        recipeWithId.recipeId = id;
        recipeWithId.servings = recipe.servings;
        recipeWithId.ingredients = new ArrayList<>(recipe.ingredients);

        return recipeWithId;
    }

    private static Recipe getLasagne() {
        Recipe lasagne = new Recipe("Lasagne", "Preparation");
        lasagne.ingredients.add(new Ingredient(new Rational(500), "g", "Hackfleisch"));
        lasagne.ingredients.add(new Ingredient(new Rational(1), null, "Zwiebel(n)"));
        lasagne.ingredients.add(new Ingredient(new Rational(2), null, "Knoblauchzehen"));
        lasagne.ingredients.add(new Ingredient(new Rational(1), null, "Bund Petersilie oder TK"));
        lasagne.ingredients.add(new Ingredient(new Rational(1), null, "EL Tomatenmark"));
        lasagne.ingredients.add(new Ingredient(new Rational(1), null, "Dose Tomaten, geschälte (800g)"));
        lasagne.ingredients.add(new Ingredient(null, null, "Etwas Rotwein"));

        return lasagne;
    }

    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet";

    private static Recipe getAntipasti() {
        Recipe antipasti = new Recipe("Antipasti", LOREM_IPSUM);
        antipasti.ingredients.add(new Ingredient(new Rational(500), "ml", "Olivenöl"));
        antipasti.ingredients.add(new Ingredient(new Rational(4), null, "Knoblauchzehen"));

        // TODO: EL ist eine Einheit.
        antipasti.ingredients.add(new Ingredient(new Rational(4), null, "EL getrocknete italienische Kräuter"));
        antipasti.servings = new Servings(4);

        return antipasti;
    }

    private static Recipe getChiliConCarne() {
        Recipe chiliConCarne = new Recipe("Chili con carne", "Preparation");
        chiliConCarne.ingredients.add(new Ingredient(new Rational(800), "g", "Rinderhackfleisch"));
        chiliConCarne.ingredients.add(new Ingredient(new Rational(1200), "g", "Tomaten, geschält"));
        chiliConCarne.ingredients.add(new Ingredient(new Rational(1), null, "Zimtstange"));
        return chiliConCarne;
    }

    public String asJson(Jsonb jsonb) {
        return jsonb.toJson(this.recipe);
    }

    public Uni<String> toUni() throws URISyntaxException, IOException {
        return Uni.createFrom().item(read());
    }

    public String read() throws URISyntaxException, IOException {
        try(InputStream in = this.getRecipeSource().toURL().openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, Charset.defaultCharset());
        }
    }
}
