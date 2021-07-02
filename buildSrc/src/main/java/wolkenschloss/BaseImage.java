package wolkenschloss;

import org.gradle.api.provider.Property;

public interface BaseImage {
    Property<String> getUrl();
    Property<String> getName();
}
