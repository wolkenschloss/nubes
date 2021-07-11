package wolkenschloss.domain;

import com.jayway.jsonpath.JsonPath;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.libvirt.Connect;
import org.libvirt.DomainInfo;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

// TODO: Refactor
import wolkenschloss.CheckedFunction;
import wolkenschloss.task.CheckedConsumer;

import java.util.ArrayList;

// TODO Kandidat für Testbed Klasse
public abstract class DomainOperations implements BuildService<DomainOperations.Params>, AutoCloseable {

    private final Connect connection;

    public interface Params extends BuildServiceParameters {
        Property<String> getDomainName();
    }

    public DomainOperations() throws LibvirtException {
        this.connection = new Connect("qemu:///system");
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
        var name = getParameters().getDomainName().get();
        var domain = connection.domainLookupByName(name);
        try {
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
        } finally {
            if (domain != null) {
                domain.free();
            }
        }
    }

    public void withInfo(CheckedConsumer<DomainInfo> consumer) throws Throwable {
        var name = getParameters().getDomainName().get();
        var domain = connection.domainLookupByName(name);
        try {
            var info = domain.getInfo();
            consumer.accept(info);
        } finally {
            if (domain != null) {
                domain.free();
            }
        }
    }

//    public <T> T withDomain(CheckedFunction<Domain, T> consumer) throws Throwable {
//
//            return consumer.apply(this.domain);
//    }
//
    public <T> void withDomain(CheckedConsumer<DomainOperations> method) throws Throwable {
        var name = getParameters().getDomainName().get();
        var domain = connection.domainLookupByName(name);
        try {
            method.accept(this);
        } finally {
            domain.free();
        }
    }

    public void create(String xml) throws LibvirtException {
        Domain domain = connection.domainDefineXML(xml);
        domain.create();
        domain.free();
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            this.connection.close();
        }
    }
}
