package wolkenschloss;

import org.gradle.api.provider.Property;

abstract public class DomainExtension {

    abstract public Property<String> getName();

    // Domain
    public abstract Property<String> getLocale();

    // Domain
    public abstract Property<String> getFqdn();
}
