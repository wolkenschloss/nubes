package family.haschka.wolkenschloss.cookbook.recipe;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class HttpParser {

    public List<String> readData(URI source) throws IOException {

        Document dom = Jsoup.parse(download(source));
        Elements scripts = dom.select("script[type=application/ld+json]");

        return scripts.stream()
                .map(Element::data)
                .collect(Collectors.toList());
    }

    private String download(URI source) throws IOException {
        try (InputStream in = source.toURL().openStream()) {
            byte[] bytes = in.readAllBytes();
            return new String(bytes, Charset.defaultCharset());
        }
    }
}
