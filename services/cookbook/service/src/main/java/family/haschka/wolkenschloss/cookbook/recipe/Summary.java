package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.Objects;

public class Summary {
    public String recipeId;
    public String title;

    public Summary(String recipeId, String title) {
        this.recipeId = recipeId;
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Summary summary = (Summary) o;
        return Objects.equals(recipeId, summary.recipeId) && Objects.equals(title, summary.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recipeId, title);
    }
}
