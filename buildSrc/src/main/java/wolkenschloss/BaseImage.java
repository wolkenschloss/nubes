package wolkenschloss;

import org.gradle.api.provider.Property;

import java.net.URL;

public interface BaseImage {
    public Property<URL> getUrl();
    public Property<String> getSha256Sum();
}
