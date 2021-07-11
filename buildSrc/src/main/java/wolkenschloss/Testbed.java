package wolkenschloss;

import org.gradle.api.provider.Property;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;

public class Testbed implements AutoCloseable {

    public Connect getConnection() {
        return this.connection;
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

    private final Connect connection;

    @SuppressWarnings("CdiInjectionPointsInspection")
    public Testbed(String name) throws LibvirtException {
        connection = new Connect("qemu:///system");
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
