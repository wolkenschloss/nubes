package wolkenschloss.domain;

import org.gradle.api.file.RegularFile;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildServiceRegistry;

abstract public class DomainExtension {

    @SuppressWarnings("UnstableApiUsage")
    public void initialize(BuildServiceRegistry sharedServices,
                           Provider<RegularFile> knownHostsFile,
                           Provider<RegularFile> hostsFile,
                           Provider<RegularFile> kubeConfig,
                           Provider<RegularFile> dockerConfig) {
        getName().convention("testbed");
//        getFqdn().convention("testbed.wolkenschloss.local");
        getDomainSuffix().convention("wolkenschloss.local");
        getLocale().convention(System.getenv("LANG"));
        getKnownHostsFile().convention(knownHostsFile);
        getHostsFile().convention(hostsFile);
        getKubeConfigFile().convention(kubeConfig);
        getDockerConfigFile().convention(dockerConfig);

        getDomainOperations().set(sharedServices.registerIfAbsent(
                "domainops",
                DomainOperations.class,
                spec -> {
                    spec.getParameters().getDomainName().set(getName());
                    spec.getParameters().getKnownHostsFile().set(getKnownHostsFile());
                }));
    }

    abstract public Property<String> getName();

    public abstract Property<String> getLocale();

//    public abstract Property<String> getFqdn();
    public String getTestbedVmFqdn() {
        return String.format("%s.%s", getName().get(), getDomainSuffix().get());
    }

    public abstract ListProperty<String> getHosts();

    public abstract Property<String> getDomainSuffix();

    abstract public RegularFileProperty getKnownHostsFile();

    abstract public RegularFileProperty getHostsFile();

    abstract public RegularFileProperty getKubeConfigFile();

    abstract public Property<DomainOperations> getDomainOperations();

    abstract public RegularFileProperty getDockerConfigFile();
}
