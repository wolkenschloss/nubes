package wolkenschloss;

import com.jayway.jsonpath.JsonPath;
import org.gradle.api.GradleException;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;

import java.util.ArrayList;
import java.util.function.Consumer;

// TODO Kandidat für Testbed Klasse
public class Domain {
    private final String name;
    private final int retries;

    public Domain(String name, int retries){
        this.name = name;
        this.retries = retries;
    }

    public String getTestbedHostAddress() throws LibvirtException, InterruptedException {

        var connection = new org.libvirt.Connect("qemu:///system");
        var domain = connection.domainLookupByName(name);

        String result = getInterfaces(domain);

        // Parse Result
        var interfaceName = "enp1s0";

        var path = String.format("$.return[?(@.name==\"%s\")].ip-addresses[?(@.ip-address-type==\"ipv4\")].ip-address", interfaceName);

        ArrayList<String> ipAddresses = JsonPath.parse(result).read(path);

        if (ipAddresses.size() != 1) {
            throw new GradleException(
                    String.format("Interface %s has %d IP-Addresses. I do not know what to do now.",
                            interfaceName,
                            ipAddresses.size()));
        }

        return ipAddresses.stream()
                .findFirst()
                .orElseThrow(() -> new GradleException("IP Adresse des Prüfstandes kann nicht ermittelt werden."));
    }

    // Erfordert die Installation des Paketes qemu-guest-agent in der VM.
    // Gebe 30 Sekunden Timeout. Der erste Start könnte etwas länger dauern.
    private String getInterfaces(org.libvirt.Domain domain) throws LibvirtException, InterruptedException {
        int retry = this.retries;
        while (true) {
            try {
                return domain.qemuAgentCommand("{\"execute\": \"guest-network-get-interfaces\"}", 30, 0);
            } catch (LibvirtException e) {
                if (--retry < 1) {
                    throw e;
                }
                Thread.sleep(30000);
            }
        }
    }

    public DomainInfo getInfo() throws LibvirtException {
        var connection = new org.libvirt.Connect("qemu:///system");
        var domain = connection.domainLookupByName(name);
        var info = domain.getInfo();
        connection.close();
        return info;
    }

    public void withPool(String name, Consumer<StoragePool> consumer) throws LibvirtException {
        var connection = new org.libvirt.Connect("qemu:///system");
        var pool = connection.storagePoolLookupByName(name);
        consumer.accept(pool);
        connection.close();
    }
}
