package wolkenschloss.domain;

import com.google.cloud.tools.jib.api.*;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.gradle.api.GradleException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class RegistryService {

    private final String name;

    public RegistryService(String ip) {
        this.name = String.format("%s:32000", ip);
    }

    public String getAddress() {
        return name;
    }

    public List<String> listCatalogs() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();

        @SuppressWarnings("HttpUrlsUsage")
        var uri = URI.create(String.format("http://%s/v2/_catalog", name));

        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new GradleException(String.format("Return Code from Registry: %d", response.statusCode()));
        }

        DocumentContext parse = JsonPath.parse(response.body());
        return parse.read("$.repositories[*]");
    }

    public String uploadImage(String image) throws InvalidImageReferenceException, CacheDirectoryCreationException, IOException, ExecutionException, InterruptedException, RegistryException {
        var tag = String.format("%s/%s", name, image);
        Jib.from("hello-world")
                .containerize(Containerizer.to(RegistryImage.named(tag))
                        .setAllowInsecureRegistries(true));
        return tag;
    }

    public <T> void withRegistry(Consumer<RegistryService> method) {
        method.accept(this);
    }

    @Override
    public String toString() {
        return "Registry{" +
                "name='" + name + '\'' +
                '}';
    }
}
