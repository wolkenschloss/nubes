package family.haschka.wolkenschloss.cookbook.recipe;

import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class HttpParser implements ResourceParser {
    @Inject
    Logger log;

    @Override
    public List<String> readData(URI source) throws IOException {
        log.infov("HttpParser.readData {0}", source.toString());


        Document dom = Jsoup.parse(source.toURL(), 10000);
        Elements scripts = dom.select("script[type=application/ld+json]");
        var result = scripts.stream()
                .map(Element::data)
                .collect(Collectors.toList());

        log.infov("Result: {0}", result);
        return result;
    }
}
