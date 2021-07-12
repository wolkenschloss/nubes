package wolkenschloss.domain;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistry;

abstract public class DomainExtension {

    public Provider<DomainOperations> initialize(BuildServiceRegistry sharedServices) {
        getName().convention("testbed");
        getFqdn().convention("testbed.wolkenschloss.local");
        getLocale().convention(System.getenv("LANG"));

        getDomainOperations().set(sharedServices.registerIfAbsent(
                "domainops",
                DomainOperations.class,
                spec -> spec.getParameters().getDomainName().set(getName())));

        return getDomainOperations();
    }

    abstract public Property<String> getName();

    public abstract Property<String> getLocale();

    public abstract Property<String> getFqdn();

    abstract public Property<DomainOperations> getDomainOperations();
}
