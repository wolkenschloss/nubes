package wolkenschloss.domain;

import com.jayway.jsonpath.JsonPath;
import org.gradle.api.GradleException;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;
import wolkenschloss.pool.Pool;
import wolkenschloss.task.CheckedConsumer;

import java.util.ArrayList;

// TODO Kandidat für Testbed Klasse
public class Domain implements AutoCloseable {

//    private final String name;
//    private final int retries;

    private final org.libvirt.Domain domain;

    public Domain(org.libvirt.Domain domain) {
        this.domain = domain;
    }



    public String getTestbedHostAddress() throws Throwable {

                String result = getInterfaces(10);

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
    private String getInterfaces(int retries) throws LibvirtException, InterruptedException {
        int retry = retries;
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

    public void withInfo(CheckedConsumer<DomainInfo> consumer) throws Throwable {
        var info = domain.getInfo();
        consumer.accept(info);
    }

    public Pool getPool(String name) {
        return new Pool(name);
    }



    @Override
    public void close() throws Exception {
        this.domain.free();
    }
}
