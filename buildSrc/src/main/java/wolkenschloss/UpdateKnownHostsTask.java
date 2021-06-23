package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleScriptException;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.libvirt.LibvirtException;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

/**
 * Erzeugt eine known_hosts Datei anhand der Adresse des Prüfstandes
 * und dem Server Schlüssel.
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

    @OutputFile
    abstract public RegularFileProperty getKnownHostsFile();

    @Input
    abstract public Property<String> getDomain();

    @Inject
    abstract protected FileSystemOperations getFileSystemOperations();

    @TaskAction
    public void update() {
        try {
            var domain = new Domain(getDomain().get(), 10);

            var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
            var attributes = PosixFilePermissions.asFileAttribute(permissions);
            var path = getKnownHostsFile().get().getAsFile().toPath();
            var file = Files.createFile(path, attributes);
            var key = Files.readString(getServerKey().get().getAsFile().toPath());

            Files.writeString(
                    file.toAbsolutePath(),
                    String.format("%s %s", domain.getTestbedHostAddress(), key),
                    StandardOpenOption.WRITE);

        } catch (LibvirtException | IOException | InterruptedException e) {
            throw new GradleScriptException("Die Datei known_hosts konnte nicht vollständig aktualisiert werden.", e);
        }
    }
}
