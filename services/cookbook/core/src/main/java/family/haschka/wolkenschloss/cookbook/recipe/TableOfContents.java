package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.List;
import java.util.Objects;

public class TableOfContents {
    public List<Summary> content;
    public long total;

    public TableOfContents(long total, List<Summary> content) {
        this.total = total;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TableOfContents that = (TableOfContents) o;
        return total == that.total && Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, total);
    }
}
