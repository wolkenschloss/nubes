package wolkenschloss.domain;

import com.jayway.jsonpath.JsonPath;
import org.gradle.api.GradleException;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

import java.util.ArrayList;
import java.util.function.Consumer;

public abstract class DomainOperations implements BuildService<DomainOperations.Params>, AutoCloseable {

    private final Connect connection;

    public interface Params extends BuildServiceParameters {
        Property<String> getDomainName();
    }

    public DomainOperations() throws LibvirtException {
        this.connection = new Connect("qemu:///system");
    }

    public String getTestbedHostAddress() throws LibvirtException, InterruptedException {

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

    public void withInfo(Consumer<DomainInfo> consumer) {
        var name = getParameters().getDomainName().get();
        try {
            Domain domain = null;
            try {
                domain = connection.domainLookupByName(name);
                var info = domain.getInfo();
                consumer.accept(info);
            } catch (LibvirtException exception) {
                throw new RuntimeException("Can not process domain info.", exception);
            } finally {
                if (domain != null) {
                    domain.free();
                }
            }
        } catch (LibvirtException exception) {
            throw new RuntimeException("Can not free domain.", exception);
        }
    }

    public <T> void withDomain(Consumer<DomainOperations> method) {
        var name = getParameters().getDomainName().get();
        Domain domain = null;
        try {
            try {
                domain = connection.domainLookupByName(name);
                method.accept(this);
            } catch (LibvirtException exception) {
                throw new RuntimeException("Can not lookup domain", exception);
            } finally {
                domain.free();
            }
        } catch (LibvirtException exception) {

            throw new RuntimeException("Can not free domain", exception);
        }
    }

    public void create(String xml) throws LibvirtException {
        Domain domain = connection.domainDefineXML(xml);
        domain.create();
        domain.free();
    }

    public boolean deleteDomainIfExists(Property<String> name) throws LibvirtException {

        var deleted = false;

        var domainIds = connection.listDomains();

        for (var domainId : domainIds) {
            var domain = connection.domainLookupByID(domainId);
            if (domain.getName().equals(name.get())) {
                domain.destroy();
                deleted = true;
            }
            domain.free();
        }

        var definedDomains = connection.listDefinedDomains();

        for (var definedDomain : definedDomains) {
            if (definedDomain.equals(name.get())) {
                var domain = connection.domainLookupByName(definedDomain);
                domain.undefine();
                deleted = true;
                domain.free();
            }
        }

        return deleted;
    }

    @Override
    public void close() throws Exception {
        if (connection != null) {
            this.connection.close();
        }
    }
}
