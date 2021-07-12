package wolkenschloss;

import org.gradle.api.provider.Property;

public abstract class HostExtension {

    public static final int DEFAULT_CALLBACK_PORT = 9191;

    public void initialize() {
        getHostAddress().convention(IpUtil.getHostAddress());
        getCallbackPort().set(DEFAULT_CALLBACK_PORT);
    }

    public abstract Property<String> getHostAddress();

    public abstract Property<Integer> getCallbackPort();
}
