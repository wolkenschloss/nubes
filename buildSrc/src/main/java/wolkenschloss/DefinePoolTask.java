package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

@CacheableTask
abstract public class DefinePoolTask extends DefaultTask {

    @Input
    public abstract Property<String> getPoolName();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getXmlDescription();

    @OutputFile
    abstract public RegularFileProperty getPoolRunFile();

    @TaskAction
    public void exec() {

        try {
            var connection = new Connect("qemu:///system");

            if (getPoolRunFile().get().getAsFile().exists()) {
                var uuid = UUID.fromString(Files.readString(getPoolRunFile().getAsFile().get().toPath()));

                getLogger().info("Ein Pool mit der ID {} ist noch vorhanden", uuid);
                var pool = connection.storagePoolLookupByUUID(uuid);

                getLogger().info("Der Pool {} pool wird zerstört.", pool.getName());
                pool.destroy();

//                getLogger().info("Der Pool {} pool wird gelöscht.", pool.getName());
//                pool.undefine();

                Files.delete(getPoolRunFile().getAsFile().get().toPath());

                getLogger().info("Die Markierung-Datei {} des Pools wurde gelöscht.",
                            getPoolRunFile().getAsFile().get().toPath());
            }

            var file = getXmlDescription().get().getAsFile().getAbsoluteFile().toPath();
            var xml = Files.readString(file);
            var pool = connection.storagePoolCreateXML(xml, 0);

            Files.writeString(getPoolRunFile().getAsFile().get().toPath(), pool.getUUIDString());

            connection.close();
        } catch (LibvirtException | IOException e) {
            e.printStackTrace();
            throw new GradleScriptException("Can not define storage pool", e);
        }
    }
}
