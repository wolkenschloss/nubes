package wolkenschloss.gradle.testbed.domain

import com.jayway.jsonpath.JsonPath
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFile
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.process.ExecOperations
import org.libvirt.Connect
import org.libvirt.Domain
import org.libvirt.DomainInfo
import org.libvirt.LibvirtException

abstract class DomainOperations : BuildService<BuildServiceParameters.None>, AutoCloseable {
    private val connection: Connect = Connect("qemu:///system")

    fun ipAddress(domainName: Provider<String>): String {
            val result = getInterfaces(domainName.get(), 10)
            val interfaceName = "enp1s0"
            val path = String.format(
                "$.return[?(@.name==\"%s\")].ip-addresses[?(@.ip-address-type==\"ipv4\")].ip-address",
                interfaceName
            )
            val ipAddresses = JsonPath.parse(result).read<ArrayList<String>>(path)
            if (ipAddresses.size != 1) {
                throw GradleException(
                    String.format(
                        "Interface %s has %d IP-Addresses. I do not know what to do now.",
                        interfaceName,
                        ipAddresses.size
                    )
                )
            }
            return ipAddresses.stream()
                .findFirst()
                .orElseThrow { GradleException("IP Adresse des Prüfstandes kann nicht ermittelt werden.") }
        }

    // Erfordert die Installation des Paketes qemu-guest-agent in der VM.
    // Gebe 30 Sekunden Timeout. Der erste Start könnte etwas länger dauern.
    private fun getInterfaces(domainName: String, retries: Int): String {
        val domain = connection.domainLookupByName(domainName)
        try {
            var retry = retries
            while (true) {
                try {
                    return domain!!.qemuAgentCommand("{\"execute\": \"guest-network-get-interfaces\"}", 30, 0)
                } catch (e: LibvirtException) {
                    if (--retry < 1) {
                        throw e
                    }
                    Thread.sleep(30000)
                }
            }
        } finally {
            domain?.free()
        }
    }

    fun withInfo(domainName: Provider<String>, consumer: (DomainInfo) -> Unit) {
        try {
            var domain: Domain? = null
            try {
                domain = connection.domainLookupByName(domainName.get())
                val info = domain.info
                consumer(info)
            } catch (exception: LibvirtException) {
                throw RuntimeException("Can not process domain info.", exception)
            } finally {
                domain?.free()
            }
        } catch (exception: LibvirtException) {
            throw RuntimeException("Can not free domain.", exception)
        }
    }

    fun withDomain(domainName: Provider<String>, method: (DomainOperations) -> Unit) {
        var domain: Domain? = null
        try {
            try {
                domain = connection.domainLookupByName(domainName.get())
                method(this)
            } catch (exception: LibvirtException) {
                throw RuntimeException("Domain does not exist", exception)
            } finally {
                domain?.free()
            }
        } catch (exception: LibvirtException) {
            throw RuntimeException("Can not free domain", exception)
        }
    }

    fun create(xml: String) {
        val domain = connection.domainDefineXML(xml)
        domain.create()
        domain.free()
    }

    fun deleteDomainIfExists(name: Property<String>): Boolean {
        var deleted = false
        val domainIds = connection.listDomains()
        for (domainId in domainIds) {
            val domain = connection.domainLookupByID(domainId)
            if (domain.name == name.get()) {
                domain.destroy()
                deleted = true
            }
            domain.free()
        }
        val definedDomains = connection.listDefinedDomains()
        for (definedDomain in definedDomains) {
            if (definedDomain == name.get()) {
                val domain = connection.domainLookupByName(definedDomain)
                domain.undefine()
                deleted = true
                domain.free()
            }
        }
        return deleted
    }

    @Throws(Throwable::class)
    fun getShell(domainName: Provider<String>, knownHostsFile: Provider<RegularFile>, execOperations: ExecOperations): SecureShellService {
        val ip = ipAddress(domainName)
        return SecureShellService(execOperations, ip, knownHostsFile)
    }

    fun withShell(domainName: Provider<String>, knownHostsFile: Provider<RegularFile>, execOperations: ExecOperations, method: (SecureShellService) -> Unit) {
        try {
            val service = SecureShellService(execOperations, ipAddress(domainName), knownHostsFile)
            method(service)
        } catch (throwable: Throwable) {
            throw GradleException("Can not create secure shell service", throwable)
        }
    }

    fun registry(registry: Provider<String>): RegistryService {
        try {
            return RegistryService(registry.get())
        } catch (throwable: Throwable) {
            throw GradleException("Can not create registry service", throwable)
        }
    }

    override fun close() {
        connection.close()
    }

    companion object {
        private const val NAME = "domain-operations"

        fun getInstance(gradle: Gradle) : Provider<DomainOperations> {
            return gradle.sharedServices.registerIfAbsent(NAME, DomainOperations::class.java) {}
        }
    }
}