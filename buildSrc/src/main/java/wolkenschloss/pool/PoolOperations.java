package wolkenschloss.pool;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PoolOperations implements BuildService<PoolOperations.Params>, AutoCloseable {

    private final Connect connection;

    public interface Params extends BuildServiceParameters {
        Property<String> getPoolName();
    }

    public PoolOperations() throws LibvirtException {
        this.connection = new Connect("qemu:///system");
    }

    public void destroyPool(RegularFileProperty poolRunFile) throws IOException, LibvirtException {
        var uuid = UUID.fromString(Files.readString(poolRunFile.getAsFile().get().toPath()));

        fallsPoolExistiert(uuid, pool -> {
            pool.destroy();
            pool.undefine();
        });

        Files.delete(poolRunFile.getAsFile().get().toPath());
    }

    public UUID createPool(RegularFileProperty xmlDescription) throws IOException, LibvirtException {
        var file = xmlDescription.get().getAsFile().getAbsoluteFile().toPath();
        var xml = Files.readString(file);

        var pool = connection.storagePoolDefineXML(xml, 0);

        try {
            pool.create(0);
            return UUID.fromString(pool.getUUIDString());
        } finally {
            pool.free();
        }
    }

    public void deletePoolIfExists(RegularFileProperty poolRunFile) throws LibvirtException, IOException {
        if (poolRunFile.get().getAsFile().exists()){
            destroyPool(poolRunFile);
        }
    }

    public List<String> allPools() throws LibvirtException {
        return Stream.concat(
                Arrays.stream(connection.listDefinedStoragePools()),
                Arrays.stream(connection.listStoragePools()))
                .collect(Collectors.toList());
    }

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

    public boolean poolExistiert(Property<String> poolName) throws LibvirtException {
        return allPools().contains(poolName.get());
    }

    public void run(Consumer<StoragePool> consumer) {
        StoragePool pool = null;

        try {
            try {
                pool = connection.storagePoolLookupByName(getParameters().getPoolName().get());
                consumer.accept(pool);
            } catch (LibvirtException exception) {
                throw new RuntimeException("Unkown storage pool", exception);
            } finally {
                if (pool != null) {
                    pool.free();
                }
            }
        } catch (LibvirtException exception) {
            throw new RuntimeException("Can not free storage pool", exception);
        }
    }

    @Override
    public void close() throws LibvirtException {
        connection.close();
    }
}
