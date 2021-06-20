package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.tasks.TaskAction;
import org.libvirt.LibvirtException;

public class StartDomainTask extends DefaultTask {

    @TaskAction
    public void exec() {
        try {
            var connection = new org.libvirt.Connect("qemu:///system");
            var domains = connection.listDefinedDomains();
            for(var domain : domains) {
                getLogger().info("Definierte Domäne: {}", domain);
            }

            var testbedDomain = connection.domainLookupByName("testbed");
            getLogger().info("Got Domain {}", testbedDomain.getName());

            getLogger().info("Anzahl definierter Domänen: {}", connection.numOfDefinedDomains());
            getLogger().info("Anzahl Domänen: {}", connection.numOfDomains());

            var intDomains = connection.listDomains();
            for (var idom : intDomains) {
                var mydom = connection.domainLookupByID(idom);
                getLogger().info("Mydom ist: id = {}, Name : {}", idom, mydom.getName());
            }

        } catch (LibvirtException e) {
            e.printStackTrace();
            throw new GradleScriptException("Fehler beim Zugriff auf libvirt", e);
        }
    }
}
