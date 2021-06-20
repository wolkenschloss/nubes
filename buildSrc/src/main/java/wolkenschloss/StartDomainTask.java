package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

abstract public class StartDomainTask extends DefaultTask {

    @Input
    abstract public Property<String> getDomain();

    @TaskAction
    public void exec() {
        try {
            var execute = getProject().findProperty("dry-run") == null;

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

            // 1. Fall Domäne existiert, ist aber ausgeschaltet.
            var domain = connection.domainLookupByName(getDomain().get());
            var info = domain.getInfo();
            var state = info.state;
            if (state == DomainInfo.DomainState.VIR_DOMAIN_SHUTOFF) {
                getLogger().info("Domäne ist vorhanden, aber ausgeschaltet.");
                getLogger().info("Domäne wird wieder eingeschaltet");
                if (execute) {
                    domain.create();
                }
            }

            // 2. Fall Domäne existiert und läuft gerade.
            if (state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
                getLogger().info("Domäne ist vorhanden und läuft gerade.");
                getLogger().info("Es wird keine Änderung durchgeführt");
            }

            // 3. Fall Domäne existiert und ist pausiert.
            if (state == DomainInfo.DomainState.VIR_DOMAIN_PAUSED) {
                getLogger().info("Domäne ist vorhanden und pausiert.");
                getLogger().info("Domäne wird wiederaufgenommen");

                if (execute) {
                    domain.resume();
                }
            }

        } catch (LibvirtException e) {
            e.printStackTrace();
            throw new GradleScriptException("Fehler beim Zugriff auf libvirt", e);
        }
    }
}
