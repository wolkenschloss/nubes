package wolkenschloss;

import org.gradle.api.provider.Property;

import java.net.URL;

public interface BaseImage {
    Property<URL> getUrl();
    Property<String> getSha256Sum();
}
