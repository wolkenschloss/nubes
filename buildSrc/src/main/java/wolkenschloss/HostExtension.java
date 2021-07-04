package wolkenschloss;

import org.gradle.api.provider.Property;

public interface HostExtension {

    // Host
    Property<String> getHostAddress();
}
