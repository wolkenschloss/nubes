package wolkenschloss;

import org.gradle.api.provider.Property;

public interface HostExtension {

    Property<String> getHostAddress();

    Property<Integer> getCallbackPort();
}
