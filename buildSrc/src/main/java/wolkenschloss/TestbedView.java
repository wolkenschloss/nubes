package wolkenschloss;

import org.gradle.api.provider.Property;

abstract public class TestbedView {

    // user?
    abstract public Property<String> getSshKey();

    // Host?
    abstract public Property<String> getHostname();

    // User
    abstract public Property<String> getUser();

    // Host
    public abstract Property<String> getHostAddress();

    // Host
    public abstract Property<Integer> getCallbackPort();

    // Domain
    public abstract Property<String> getLocale();

    // Domain
    public abstract Property<String> getFqdn();
}
