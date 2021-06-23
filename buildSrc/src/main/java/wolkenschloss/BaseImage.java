package wolkenschloss;

import org.gradle.api.provider.Property;

import java.net.URL;

public interface BaseImage {
    Property<String> getUrl();
    Property<String> getSha256Sum();
}
