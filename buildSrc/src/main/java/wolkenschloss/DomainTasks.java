package wolkenschloss;

import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import wolkenschloss.domain.DomainExtension;

public class DomainTasks {

    public final DomainExtension domain;
    public final Provider<RegularFile> kubeConfig;
    public final Provider<Integer> port;

    public DomainTasks(DomainExtension domain, Provider<RegularFile> kubeConfig, Provider<Integer> port) {

        this.domain = domain;
        this.kubeConfig = kubeConfig;
        this.port = port;
    }
}
