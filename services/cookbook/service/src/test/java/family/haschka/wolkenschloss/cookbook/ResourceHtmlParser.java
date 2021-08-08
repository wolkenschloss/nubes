package family.haschka.wolkenschloss.cookbook;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ResourceHtmlParser implements ResourceParser {

    public ResourceHtmlParser() {
    }

    @Override
    public List<String> readData(URI source) throws IOException {
        try (var input = this.getClass().getClassLoader().getResourceAsStream(source.getPath())) {
            Document dom = Jsoup.parse(input, "UTF-8", "");
            Elements scripts = dom.select("script[type=application/ld+json]");
            return scripts.stream()
                    .map(Element::data)
                    .collect(Collectors.toList());
        }
    }
}
