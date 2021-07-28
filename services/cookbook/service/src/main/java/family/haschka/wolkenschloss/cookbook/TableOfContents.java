package family.haschka.wolkenschloss.cookbook;

import java.util.List;

public class TableOfContents {
    public List<BriefDescription> content;
    public long total;

    public TableOfContents(long total, List<BriefDescription> content) {

        this.total = total;
        this.content = content;
    }
}
