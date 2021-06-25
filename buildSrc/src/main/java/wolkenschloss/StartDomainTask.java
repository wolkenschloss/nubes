package wolkenschloss;

import com.sun.net.httpserver.HttpServer;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.libvirt.LibvirtException;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@CacheableTask
abstract public class StartDomainTask extends DefaultTask {

    @Input
    abstract public Property<String> getDomain();

    @Input
    abstract public Property<String> getHostname();

    @Internal
    abstract public Property<Integer> getPort();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getXmlDescription();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getPoolRunFile();

    @Inject
    abstract protected ExecOperations getExecOperations();

    @OutputFile
    abstract public RegularFileProperty getKnownHostsFile();

    @TaskAction
    public void exec() throws IOException {
        try {
//            var execute = getProject().findProperty("dry-run") == null;

            // TODO Kandidat für Testbed Klasse
            var connection = new org.libvirt.Connect("qemu:///system");

            try {

                getLogger().info("Executing 'virsh define {}'", getXmlDescription().get());

                var xml = Files.readString(getXmlDescription().get().getAsFile().toPath());
                connection.domainCreateXML(xml, 0);
                var serverKey = waitForCallback();
                updateKnownHosts(serverKey);

//                // A. Domain ist definiert
//                var domain = connection.domainLookupByName(getDomain().get());
//                var info = domain.getInfo();
//                var state = info.state;
//
//                // 1. Fall Domäne existiert, ist aber ausgeschaltet.
//                if (state == DomainInfo.DomainState.VIR_DOMAIN_SHUTOFF) {
//                    getLogger().info("Domäne ist vorhanden, aber ausgeschaltet.");
//                    getLogger().info("Domäne wird wieder eingeschaltet");
//                    if (execute) {
//                        domain.create();
//                    }
//                }
//
//                // 2. Fall Domäne existiert und läuft gerade.
//                if (state == DomainInfo.DomainState.VIR_DOMAIN_RUNNING) {
//                    getLogger().info("Domäne ist vorhanden und läuft gerade.");
//                    getLogger().info("Es wird keine Änderung durchgeführt");
//                }
//
//                // 3. Fall Domäne existiert und ist pausiert.
//                if (state == DomainInfo.DomainState.VIR_DOMAIN_PAUSED) {
//                    getLogger().info("Domäne ist vorhanden und pausiert.");
//                    getLogger().info("Domäne wird wiederaufgenommen");
//
//                    if (execute) {
//                        domain.resume();
//                    }
//                }
//
//                // B. Domain

            } finally {
                connection.close();
            }
        } catch (LibvirtException e) {
            e.printStackTrace();
            throw new GradleScriptException("Fehler beim Zugriff auf libvirt", e);
        }
    }

    public void updateKnownHosts(String serverKey) {
        try {
            var domain = new Domain(getDomain().get(), 10);

            var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
            var attributes = PosixFilePermissions.asFileAttribute(permissions);
            var path = getKnownHostsFile().get().getAsFile().toPath();
            var file = Files.createFile(path, attributes);

            Files.writeString(
                    file.toAbsolutePath(),
                    String.format("%s %s", domain.getTestbedHostAddress(), serverKey),
                    StandardOpenOption.WRITE);

        } catch (LibvirtException | IOException | InterruptedException e) {
            throw new GradleScriptException("Die Datei known_hosts konnte nicht vollständig aktualisiert werden.", e);
        }
    }

    public String waitForCallback() {
        try {
            var executor = Executors.newSingleThreadExecutor();
            BlockingQueue<String> serverKeyResult = new SynchronousQueue<>();

            var server = HttpServer.create(new InetSocketAddress(getPort().get()), 0);
            server.createContext(String.format("/%s", getHostname().get()), new CallbackHandler(serverKeyResult, getLogger()));

            server.setExecutor(executor);
            server.start();
            getLogger().lifecycle("Waiting for connection from testbed");

            try {
                var serverKey = serverKeyResult.poll(10, TimeUnit.MINUTES);

                if (serverKey != null) {
                    return serverKey;
                } else {
                    throw new GradleException("Did not receive call from testbed");
                }
            } catch (InterruptedException exception) {
                throw new GradleException("Premature termination while waiting for the callback");
            } finally {
                executor.shutdown();
                server.stop(0);
            }
        } catch (IOException e) {
            throw new GradleScriptException("Can not start webserver", e);
        }
    }
}
