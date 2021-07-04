package wolkenschloss;

import org.gradle.api.provider.Property;

public interface BaseImageExtension {
    Property<String> getUrl();
    Property<String> getName();
}
