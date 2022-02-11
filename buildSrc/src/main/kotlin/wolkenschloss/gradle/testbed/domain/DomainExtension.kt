package wolkenschloss.gradle.testbed.domain

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceRegistry

abstract class DomainExtension {
    fun initialize(
        sharedServices: BuildServiceRegistry,
        runDirectory: DirectoryProperty
    ) {
        name.convention("testbed")
        domainSuffix.convention("wolkenschloss.local")
        locale.convention(System.getenv("LANG"))
        knownHostsFile.convention(runDirectory.file(DEFAULT_KNOWN_HOSTS_FILE_NAME))
        hostsFile.convention(runDirectory.file(DEFAULT_HOSTS_FILE_NAME))
        kubeConfigFile.convention(runDirectory.file(DEFAULT_KUBE_CONFIG_FILE_NAME))
        dockerConfigFile.convention(runDirectory.file(DEFAULT_DOCKER_CONFIG_FILE_NAME))

        domainOperations.set(
            sharedServices.registerIfAbsent(
                "domainops",
                DomainOperations::class.java) {
                parameters.domainName.set(name)
                parameters.knownHostsFile.set(runDirectory.file(DEFAULT_KNOWN_HOSTS_FILE_NAME))
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

    companion object {
        const val DEFAULT_KNOWN_HOSTS_FILE_NAME = "known_hosts"
        const val DEFAULT_HOSTS_FILE_NAME = "hosts"
        const val DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig"
        const val DEFAULT_DOCKER_CONFIG_FILE_NAME = "docker/config.json"
    }
}