package family.haschka.wolkenschloss.cookbook.recipe;

import java.util.List;

public class TableOfContents {
    public List<Summary> content;
    public long total;

    public TableOfContents(long total, List<Summary> content) {
        this.total = total;
        this.content = content;
    }
}
