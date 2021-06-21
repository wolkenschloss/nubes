package wolkenschloss;

import com.jayway.jsonpath.JsonPath;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

/**
 * Aktualisiert die Datei $HOME/.ssh/known_hosts
 * <p>
 * Falls bereits Schlüssel für den Prüfstand in der Datei enthalten ist,
 * wird dieser gelöscht.
 * <p>
 * Der aktuelle Schlüssel des Prüfstandes wird in die Datei geschrieben.
 * <p>
 * Eine Kopie der Originaldatei wird in run/known_hosts.bak gesichert.
 *
 * Die korrekte Ausführung dieser Aufgabe erfordert einen Hack. Der
 * qemu-guest-agent, welcher normalerweise automatisch durch systemd
 * gestartet wird, sobald eine Anfrage an den Agent gestellt wird,
 * startet nicht automatisch nach der Installation. Deshalb ist in der
 * user-data Konfiguration die Startanweisung:
 *
 * runcmd:
 *   - systemctl start qemu-user-agent
 *
 * explizit aufgeführt. Das startet den Agent. Nach einem Neustart der
 * Maschine läuft der Mechanismus normal. Das Anschubsen durch systemctl
 * start ist dann nicht mehr erforderlich.
 */
public abstract class UpdateKnownHostsTask extends DefaultTask {

    @InputFile
    abstract public RegularFileProperty getServerKey();

    @Internal
    abstract public RegularFileProperty getOriginalKnownHostsFile();

    @Internal
    abstract public DirectoryProperty getCopyOfKnownHostsDir();

    @Input
    abstract public Property<String> getDomain();

    @Inject
    abstract protected ExecOperations getExecOperations();

    @Inject
    abstract protected FileSystemOperations getFileSystemOperations();

    @TaskAction
    public void update() {
        try {
            var host = getTestbedHostAddress();

            // Backup known_hosts
            getFileSystemOperations().copy(c -> {
                c.from(getOriginalKnownHostsFile().get());

                c.into(getCopyOfKnownHostsDir().get());
            });

            // Remove old entry
            var result = getExecOperations().exec(e -> {
                e.commandLine("ssh-keygen");
                e.args("-R", host);
            });

            var serverKey = Files.readString(getServerKey().get().getAsFile().toPath());

            // Update known_hosts
            Files.write(
                    getOriginalKnownHostsFile().get().getAsFile().toPath(),
                    String.format("%s %s%n", host, serverKey).getBytes(),
                    StandardOpenOption.APPEND);

        } catch (LibvirtException | IOException | InterruptedException e) {
            throw new GradleScriptException("Die Datei known_hosts konnte nicht vollständig aktualisiert werden.", e);
        }
    }

    private String getTestbedHostAddress() throws LibvirtException, InterruptedException {

        var connection = new org.libvirt.Connect("qemu:///system");
        var domain = connection.domainLookupByName(getDomain().get());
        String result = getInterfaces(domain);

        // Parse Result
        var interfaceName = "enp1s0";

        var path = String.format("$.return[?(@.name==\"%s\")].ip-addresses[?(@.ip-address-type==\"ipv4\")].ip-address", interfaceName);

        ArrayList<String> ipAddresses = JsonPath.parse(result).read(path);

        if (ipAddresses.size() != 1) {
            throw new GradleException(
                    String.format("Interface %s has %d IP-Addresses. I do not know what to do now.",
                            interfaceName,
                            ipAddresses.size()));
        }

        ipAddresses.forEach(address -> {
            getLogger().info("Got IP-Address {}", address);
        });

        return ipAddresses.stream()
                .findFirst()
                .orElseThrow(() -> new GradleException("IP Adresse des Prüfstandes kann nicht ermittelt werden."));
    }

    // Erfordert die Installation des Paketes qemu-guest-agent in der VM.
    // Gebe 30 Sekunden Timeout. Der erste Start könnte etwas länger dauern.
    private String getInterfaces(Domain domain) throws LibvirtException, InterruptedException {
        int retry = 10;
        while (true) {
            try {
                return domain.qemuAgentCommand("{\"execute\": \"guest-network-get-interfaces\"}", 30, 0);
            } catch (LibvirtException e) {
                getLogger().info("Error connecting qemu guest agent. Retries left: {}", retry);
                if (--retry == 0) {
                    throw e;
                }
                Thread.sleep(30000);
            }
        }
    }
}
