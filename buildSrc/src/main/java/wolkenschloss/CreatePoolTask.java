package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CacheableTask
abstract public class CreatePoolTask extends DefaultTask {

    @Input
    public abstract Property<String> getPoolName();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getXmlDescription();

    @OutputFile
    abstract public RegularFileProperty getPoolRunFile();

    @FunctionalInterface
    interface StoragePoolConsumer {
        void accept(StoragePool pool) throws LibvirtException;
    }

    private boolean poolExistiert(Connect connection, String poolName) throws LibvirtException {
        return allPools(connection).contains(poolName);
    }

    private List<String> allPools(Connect connection) throws LibvirtException {
        return Stream.concat(
                Arrays.stream(connection.listDefinedStoragePools()),
                Arrays.stream(connection.listStoragePools()))
                .collect(Collectors.toList());
    }

    private void fallsPoolExistiert(Connect connection, UUID poolId, StoragePoolConsumer consumer) throws LibvirtException {
        for (var poolName : allPools(connection)) {
            var pool = connection.storagePoolLookupByName(poolName);

            try {
                if (pool.getUUIDString().equals(poolId.toString())) {
                    getLogger().info("Process defined storage pool {}", poolName);
                    consumer.accept(pool);
                }
            } finally {
                pool.free();
            }
        }
    }

    @TaskAction
    public void exec() {

        try {
            var connection = new Connect("qemu:///system");
            try {
                if (getPoolRunFile().get().getAsFile().exists()) {
                    var uuid = UUID.fromString(Files.readString(getPoolRunFile().getAsFile().get().toPath()));

                    getLogger().info("Ein Pool mit der ID {} ist noch vorhanden", uuid);

                    fallsPoolExistiert(connection, uuid, pool -> {
                        getLogger().info("Der Pool {} pool wird zerstört.", pool.getName());
                        pool.destroy();
                        pool.undefine();
                    });

                    Files.delete(getPoolRunFile().getAsFile().get().toPath());

                    getLogger().info("Die Markierung-Datei {} des Pools wurde gelöscht.",
                            getPoolRunFile().getAsFile().get().toPath());
                }

                if (poolExistiert(connection, getPoolName().get())) {
                    var message = String.format(
                            "Der Pool %1$s existiert bereits, aber die Markierung-Datei %2$s ist nicht vorhanden.%n" +
                                    "Löschen Sie ggf. den Storage Pool mit dem Befehl 'virsh pool-destroy %1$s && virsh pool-undefine %1$s'",
                            getPoolName().get(),
                            getPoolRunFile().get().getAsFile().getPath());

                    throw new GradleException(message);
                }

                var file = getXmlDescription().get().getAsFile().getAbsoluteFile().toPath();
                var xml = Files.readString(file);

                var pool = connection.storagePoolDefineXML(xml, 0);

                try {
                    pool.create(0);
                    Files.writeString(getPoolRunFile().getAsFile().get().toPath(), pool.getUUIDString());
                } finally {
                    pool.free();
                }
            } finally {
                connection.close();
            }
        } catch (LibvirtException | IOException e) {
            throw new GradleScriptException("Can not define storage pool", e);
        }
    }
}
