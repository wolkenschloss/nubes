package wolkenschloss.gradle.testbed.domain

import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildServiceRegistry

abstract class DomainExtension {
    fun initialize(
        sharedServices: BuildServiceRegistry,
        knownHostsFile: Provider<RegularFile>,
        hostsFile: Provider<RegularFile>,
        kubeConfig: Provider<RegularFile>,
        dockerConfig: Provider<RegularFile>
    ) {
        name.convention("testbed")
        //        getFqdn().convention("testbed.wolkenschloss.local");
        domainSuffix.convention("wolkenschloss.local")
        locale.convention(System.getenv("LANG"))
        this.knownHostsFile.convention(knownHostsFile)
        this.hostsFile.convention(hostsFile)
        kubeConfigFile.convention(kubeConfig)
        dockerConfigFile.convention(dockerConfig)
        domainOperations.set(
            sharedServices.registerIfAbsent(
                "domainops",
                DomainOperations::class.java) {
                parameters.domainName.set(name)
                parameters.knownHostsFile.set(knownHostsFile)
            }
        )
    }

    abstract val name: Property<String?>
    abstract val locale: Property<String?>

    //    public abstract Property<String> getFqdn();
    val testbedVmFqdn: String
        get() = String.format("%s.%s", name.get(), domainSuffix.get())
    abstract val hosts: ListProperty<String>
    abstract val domainSuffix: Property<String>
    abstract val knownHostsFile: RegularFileProperty
    abstract val hostsFile: RegularFileProperty
    abstract val kubeConfigFile: RegularFileProperty
    abstract val domainOperations: Property<DomainOperations>
    abstract val dockerConfigFile: RegularFileProperty
}