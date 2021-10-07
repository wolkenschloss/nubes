package family.haschka.wolkenschloss.cookbook.ingredient;

import java.util.List;
import java.util.Objects;

public class TableOfContents {

    private final long count;
    private final List<Ingredient> content;

    public TableOfContents(long count, List<Ingredient> content) {

        this.count = count;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableOfContents that = (TableOfContents) o;
        return count == that.count && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, content);
    }
}
