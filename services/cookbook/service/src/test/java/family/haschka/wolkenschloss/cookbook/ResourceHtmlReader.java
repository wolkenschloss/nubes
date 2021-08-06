package family.haschka.wolkenschloss.cookbook;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class ResourceHtmlReader {
    private final String filename;

    public ResourceHtmlReader(String filename) {
        this.filename = filename;
    }

    List<String> read() throws IOException {
        try (var input = this.getClass().getClassLoader().getResourceAsStream(this.filename)) {
            Document dom = Jsoup.parse(input, "UTF-8", "");
            Elements scripts = dom.select("script[type=application/ld+json]");
            return scripts.stream()
                    .map(Element::data)
                    .collect(Collectors.toList());
        }
    }
}
