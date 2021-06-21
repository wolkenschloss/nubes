package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;

abstract public class DefinePoolTask extends DefaultTask {

    @Input
    public abstract Property<String> getPoolName();

    @Input
    abstract public RegularFileProperty getXmlDescription();

    @TaskAction
    public void exec() {

        try {
            var connection = new Connect("qemu:///system");

            var definedPools = connection.listDefinedStoragePools();
            var runningPools = connection.listStoragePools();
            var allPools = new HashSet<String>();
            Collections.addAll(allPools, definedPools);
            Collections.addAll(allPools, runningPools);

            if (allPools.contains(getPoolName().get())) {
                getLogger().info("Der Pool {} existiert bereits.", getPoolName().get());
                return;
            }

            var file = getXmlDescription().get().getAsFile().getAbsoluteFile().toPath();
            var xml = Files.readString(file);
            connection.storagePoolDefineXML(xml, 0);
            connection.close();
        } catch (LibvirtException | IOException e) {
            e.printStackTrace();
            throw new GradleScriptException("Can not define storage pool", e);
        }
    }
}
