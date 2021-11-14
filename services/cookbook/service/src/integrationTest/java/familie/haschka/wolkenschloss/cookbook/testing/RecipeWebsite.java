package familie.haschka.wolkenschloss.cookbook.testing;

import org.mockserver.client.MockServerClient;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

public class RecipeWebsite {

    private final MockServerClient client;

    RecipeWebsite(MockServerClient client) {
        this.client = client;
    }

    public void serve() {
        client.reset()
                .when(HttpRequest.request().withMethod("GET"))
                .respond((HttpRequest request) -> {
                    var path = request.getPath();
                    var file = new FileResource(path.getValue().replaceAll("^/", ""));
                    try {
                        return HttpResponse.response()
                                .withBody(file.read())
                                .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON)
                                .withStatusCode(Response.Status.OK.getStatusCode());
                    } catch (Exception e) {
                        return HttpResponse.response().withStatusCode(Response.Status.NOT_FOUND.getStatusCode());
                    }
                });
    }

    public static final String SCHEME = "http";

    public static UriBuilder orderUriFrom(String path) {
        return UriBuilder.fromPath(path)
                .scheme(SCHEME)
                .host(System.getProperty(MockServerResource.SERVER_HOST_CONFIG))
                .port(Integer.parseInt(System.getProperty(MockServerResource.SERVER_PORT_CONFIG)));
    }
}
