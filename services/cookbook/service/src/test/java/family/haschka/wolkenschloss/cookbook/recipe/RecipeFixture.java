package family.haschka.wolkenschloss.cookbook.recipe;

import java.net.URI;
import java.net.URISyntaxException;

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

    private static Recipe getLasagne() {
        Recipe lasagne = new Recipe("Lasagne", "Preparation");
        lasagne.ingredients.add(new Ingredient(500L, "g", "Hackfleisch"));
        lasagne.ingredients.add(new Ingredient(1L, null, "Zwiebeln(n)"));
        lasagne.ingredients.add(new Ingredient(2L, null, "Knoblauchzehen"));
        lasagne.ingredients.add(new Ingredient(1L, null, "Bund Petersilie oder TK"));
        lasagne.ingredients.add(new Ingredient(1L, null, "EL Tomatenmark"));
        lasagne.ingredients.add(new Ingredient(1L, null, "Dose Tomaten, geschälte (800g)"));
        lasagne.ingredients.add(new Ingredient(null, null, "Etwas Rotwein"));

        return lasagne;
    }

    public static final String LOREM_IPSUM = "Lorem ipsum dolor sit amet";

    private static Recipe getAntipasti() {
        Recipe antipasti = new Recipe("Antipasti", LOREM_IPSUM);
        antipasti.ingredients.add(new Ingredient(500L, "ml", "Olivenöl"));
        antipasti.ingredients.add(new Ingredient(4L, "", "Knoblauchzehen"));
        antipasti.ingredients.add(new Ingredient(4L, "EL", "getrocknete italienische Kräuter"));
        antipasti.servings = new Servings(4);

        return antipasti;
    }

    private static Recipe getChiliConCarne() {
        Recipe chiliConCarne = new Recipe("Chili con carne", "Preparation");
        chiliConCarne.ingredients.add(new Ingredient(800L, "g", "Rinderhackfleisch"));
        chiliConCarne.ingredients.add(new Ingredient(1200L, "g", "Tomaten, geschält"));
        chiliConCarne.ingredients.add(new Ingredient(1L, null, "Zimtstange"));
        return chiliConCarne;
    }
}