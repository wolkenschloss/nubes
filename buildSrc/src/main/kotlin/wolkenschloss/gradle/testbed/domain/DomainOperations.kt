package wolkenschloss.gradle.testbed.domain

import com.jayway.jsonpath.JsonPath
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.process.ExecOperations
import org.libvirt.Connect
import org.libvirt.Domain
import org.libvirt.DomainInfo
import org.libvirt.LibvirtException
import java.lang.Exception
import java.lang.RuntimeException
import java.util.ArrayList
import java.util.function.Consumer

abstract class DomainOperations : BuildService<DomainOperations.Params?>, AutoCloseable {
    private val connection: Connect = Connect("qemu:///system")

    interface Params : BuildServiceParameters {
        val domainName: Property<String>
        val knownHostsFile: RegularFileProperty
    }

    @get:Throws(LibvirtException::class, InterruptedException::class)
    val ipAddress: String
        get() {
            val result = getInterfaces(10)
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
    @Throws(LibvirtException::class, InterruptedException::class)
    private fun getInterfaces(retries: Int): String {
        val name = parameters.domainName.get()
        val domain = connection.domainLookupByName(name)
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

    fun withInfo(consumer: Consumer<DomainInfo>) {
        val name = parameters.domainName.get()
        try {
            var domain: Domain? = null
            try {
                domain = connection.domainLookupByName(name)
                val info = domain.info
                consumer.accept(info)
            } catch (exception: LibvirtException) {
                throw RuntimeException("Can not process domain info.", exception)
            } finally {
                domain?.free()
            }
        } catch (exception: LibvirtException) {
            throw RuntimeException("Can not free domain.", exception)
        }
    }

    fun <T> withDomain(method: Consumer<DomainOperations>) {
        val name = parameters.domainName.get()
        var domain: Domain? = null
        try {
            try {
                domain = connection.domainLookupByName(name)
                method.accept(this)
            } catch (exception: LibvirtException) {
                throw RuntimeException("Can not lookup domain", exception)
            } finally {
                domain?.free()
            }
        } catch (exception: LibvirtException) {
            throw RuntimeException("Can not free domain", exception)
        }
    }

    @Throws(LibvirtException::class)
    fun create(xml: String) {
        val domain = connection.domainDefineXML(xml)
        domain.create()
        domain.free()
    }

    @Throws(LibvirtException::class)
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
    fun getShell(execOperations: ExecOperations): SecureShellService {
        val ip = ipAddress
        return SecureShellService(execOperations, ip, parameters.knownHostsFile)
    }

    fun withShell(execOperations: ExecOperations): Consumer<Consumer<SecureShellService>> {
        return Consumer { fn: Consumer<SecureShellService> ->
            val shell: SecureShellService = try {
                SecureShellService(execOperations, ipAddress, parameters.knownHostsFile)
            } catch (throwable: Throwable) {
                throw RuntimeException(throwable)
            }
            fn.accept(shell)
        }
    }

    val registry: RegistryService
        get() = try {
            RegistryService(ipAddress)
        } catch (throwable: Throwable) {
            throw GradleException("Can not create registry service", throwable)
        }

    @Throws(Exception::class)
    override fun close() {
        connection.close()
    }
}