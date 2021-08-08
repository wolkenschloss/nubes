package family.haschka.wolkenschloss.cookbook;

import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class HttpParser implements ResourceParser {
    @Inject
    Logger log;

    @Override
    public List<String> readData(URI source) throws IOException {
        log.infov("HttpParser.readData {0}", source.toString());


        var client = HttpClient.newHttpClient();
        var request =HttpRequest.newBuilder().GET().uri(source).build();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info(response.body());

            Document dom = Jsoup.parse(response.body());
            Elements scripts = dom.select("script[type=application/ld+json]");
            var result = scripts.stream()
                    .map(Element::data)
                    .collect(Collectors.toList());

            log.infov("Result: {0}", result);
            return result;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Not implemented");
    }
}
