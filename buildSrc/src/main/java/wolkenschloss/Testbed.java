package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.process.ExecOperations;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import wolkenschloss.task.CheckedConsumer;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Testbed implements AutoCloseable {

    private final String name;

    public void destroyPool(RegularFileProperty poolRunFile) throws IOException, LibvirtException {
        var uuid = UUID.fromString(Files.readString(poolRunFile.getAsFile().get().toPath()));

        fallsPoolExistiert(uuid, pool -> {
            pool.destroy();
            pool.undefine();
        });

        Files.delete(poolRunFile.getAsFile().get().toPath());
    }

    public UUID createPool(RegularFileProperty xmlDescription, RegularFileProperty poolRunFile) throws IOException, LibvirtException {
        var file = xmlDescription.get().getAsFile().getAbsoluteFile().toPath();
        var xml = Files.readString(file);

        var pool = getConnection().storagePoolDefineXML(xml, 0);

        try {
            pool.create(0);
            return UUID.fromString(pool.getUUIDString());
        } finally {
            pool.free();
        }
    }

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

    public void deletePoolIfExists(RegularFileProperty poolRunFile) throws LibvirtException, IOException {
        if (poolRunFile.get().getAsFile().exists()){
            destroyPool(poolRunFile);
        }
    }

    @FunctionalInterface
    interface StoragePoolConsumer {
        void accept(StoragePool pool) throws LibvirtException;
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

    public List<String> allPools() throws LibvirtException {
        return Stream.concat(
                Arrays.stream(connection.listDefinedStoragePools()),
                Arrays.stream(connection.listStoragePools()))
                .collect(Collectors.toList());
    }

    // TODO Kandidat für Testbed Klasse
    public void fallsPoolExistiert(UUID poolId, StoragePoolConsumer consumer) throws LibvirtException {
        for (var poolName : allPools()) {
            var pool = connection.storagePoolLookupByName(poolName);

            try {
                if (pool.getUUIDString().equals(poolId.toString())) {
                    consumer.accept(pool);
                }
            } finally {
                pool.free();
            }
        }
    }

    // TODO Kandidat für Testbed Klasse
    public boolean poolExistiert(Property<String> poolName) throws LibvirtException {
        return allPools().contains(poolName.get());
    }

    public <T> T withDomain(CheckedFunction<Domain, T> consumer) throws Throwable {

        try(var domain = new Domain(this, this.name)) {
            return consumer.apply(domain);
        }
    }

    public <T> void withDomain(CheckedConsumer<Domain> method) throws Throwable {
        try(var domain = new Domain(this, this.name)) {
            method.accept(domain);
        }
    }



    public SecureShell getExec(ExecOperations operations, RegularFileProperty knownHosts){
        return new SecureShell(this, operations, knownHosts);
    }
}
