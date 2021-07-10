package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.process.ExecOperations;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import wolkenschloss.model.SecureShell;
import wolkenschloss.task.CheckedConsumer;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import wolkenschloss.status.Registry;
import wolkenschloss.pool.PoolOperations;
import wolkenschloss.pool.StoragePoolConsumer;

// TODO: Refactor
import wolkenschloss.domain.Domain;

public class Testbed implements AutoCloseable {

    private final String name;

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
        this.name = name;
        connection = new Connect("qemu:///system");
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public <T> T withDomain(CheckedFunction<Domain, T> consumer) throws Throwable {

        try(var domain = new Domain(this.connection.domainLookupByName(this.name))) {
            return consumer.apply(domain);
        }
    }

    public <T> void withDomain(CheckedConsumer<Domain> method) throws Throwable {
        try(var domain = new Domain(this.connection.domainLookupByName(this.name))) {
            method.accept(domain);
        }
    }

    public SecureShell getExec(ExecOperations operations, RegularFileProperty knownHosts) throws Throwable {
        var ip = this.withDomain(Domain::getTestbedHostAddress);
        return new SecureShell(ip, operations, knownHosts);
    }

    public Registry getRegistry() throws Throwable {
        var ip = this.withDomain(Domain::getTestbedHostAddress);
        var registry = new Registry(String.format("%s:32000", ip));

        return registry.connect();
    }

    public void withRegistry(CheckedConsumer<Registry> consumer) throws Throwable {
        consumer.accept(getRegistry());
    }
}
