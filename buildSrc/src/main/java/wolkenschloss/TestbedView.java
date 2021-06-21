package wolkenschloss;

import org.gradle.api.provider.Property;

abstract public class TestbedView {

    abstract public Property<String> getSshKey();
    abstract public Property<String> getHostname();
    abstract public Property<String> getUser();

    public abstract Property<String> getHostAddress();
    public abstract Property<Integer> getCallbackPort();
    public abstract Property<String> getLocale();
    public abstract Property<String> getFqdn();
}
