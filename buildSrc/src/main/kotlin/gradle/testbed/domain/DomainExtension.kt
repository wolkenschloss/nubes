package family.haschka.wolkenschloss.gradle.testbed.domain

import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskProvider
import family.haschka.wolkenschloss.gradle.ca.TrustAnchor

abstract class DomainExtension {
    fun initialize(runDirectory: DirectoryProperty, ca: TaskProvider<TrustAnchor>) {
        val domainSuffix = System.getProperty(DOMAIN_SUFFIX_PROPERTY)
        if (domainSuffix.isNullOrEmpty()) {
            throw GradleException(ERROR_DOMAIN_SUFFIX_NOT_SET)
        }

        name.convention("testbed")
        this.domainSuffix.convention(domainSuffix)
        locale.convention(System.getenv("LANG"))
        knownHostsFile.convention(runDirectory.file(DEFAULT_KNOWN_HOSTS_FILE_NAME))
        hostsFile.convention(runDirectory.file(DEFAULT_HOSTS_FILE_NAME))
        kubeConfigFile.convention(runDirectory.file(DEFAULT_KUBE_CONFIG_FILE_NAME))
        dns.convention(listOf(DEFAULT_DNS))
        certManagerVersion.convention("v1.7.1")
        privateKey.set(ca.flatMap { it.privateKey })
        certificate.set(ca.flatMap { it.certificate })
        disk.convention(DEFAULT_DISK_SIZE)
        mem.convention(DEFAULT_MEMORY_AMOUNT)
        cpus.convention(DEFAULT_NUMBER_OF_CPUS)
        image.convention(DEFAULT_IMAGE)
    }


    abstract val privateKey: RegularFileProperty
    abstract val certificate: RegularFileProperty
    abstract val certManagerVersion: Property<String>
    abstract val name: Property<String?>
    abstract val locale: Property<String>

    val testbedVmFqdn: String
        get() = String.format("%s.%s", name.get(), domainSuffix.get())

    abstract val hosts: ListProperty<String>
    abstract val domainSuffix: Property<String>
    abstract val knownHostsFile: RegularFileProperty
    abstract val hostsFile: RegularFileProperty
    abstract val kubeConfigFile: RegularFileProperty
    abstract val dns: ListProperty<String>

    abstract val disk: Property<String>
    abstract val mem: Property<String>
    abstract val cpus: Property<Int>
    abstract val image: Property<String>

    companion object {
        const val DEFAULT_IMAGE = "focal"
        const val DEFAULT_NUMBER_OF_CPUS = 2
        const val DEFAULT_MEMORY_AMOUNT = "4G"
        const val DEFAULT_DISK_SIZE = "20G"
        const val DEFAULT_DNS = "8.8.8.8"
        const val DEFAULT_KNOWN_HOSTS_FILE_NAME = "known_hosts"
        const val DEFAULT_HOSTS_FILE_NAME = "hosts"
        const val DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig"
        const val DOMAIN_SUFFIX_PROPERTY = "wolkenschloss.domain-suffix"
        const val ERROR_DOMAIN_SUFFIX_NOT_SET = "System property '$DOMAIN_SUFFIX_PROPERTY' not set"
    }
}