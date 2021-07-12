package wolkenschloss.domain;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistry;

abstract public class DomainExtension {

    public Provider<DomainOperations> initialize(BuildServiceRegistry sharedServices, Provider<RegularFile> knownHostsFile) {
        getName().convention("testbed");
        getFqdn().convention("testbed.wolkenschloss.local");
        getLocale().convention(System.getenv("LANG"));
        getKnownHostsFile().convention(knownHostsFile);

        getDomainOperations().set(sharedServices.registerIfAbsent(
                "domainops",
                DomainOperations.class,
                spec -> {
                    spec.getParameters().getDomainName().set(getName());
                    spec.getParameters().getKnownHostsFile().set(getKnownHostsFile());
                }));

        return getDomainOperations();
    }

    abstract public Property<String> getName();

    public abstract Property<String> getLocale();

    public abstract Property<String> getFqdn();

    abstract public RegularFileProperty getKnownHostsFile();

    abstract public Property<DomainOperations> getDomainOperations();
}
