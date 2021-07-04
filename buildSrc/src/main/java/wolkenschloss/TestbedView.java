package wolkenschloss;

import org.gradle.api.provider.Property;

abstract public class TestbedView {

    // user?
    abstract public Property<String> getSshKey();

//    // Host? Domain?
//    abstract public Property<String> getHostname();

    // User
    abstract public Property<String> getUser();






}
