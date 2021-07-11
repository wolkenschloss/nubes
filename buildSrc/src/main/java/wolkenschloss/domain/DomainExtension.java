package wolkenschloss.domain;

import org.gradle.api.provider.Property;

abstract public class DomainExtension {

    abstract public Property<String> getName();

    public abstract Property<String> getLocale();

    public abstract Property<String> getFqdn();
}
