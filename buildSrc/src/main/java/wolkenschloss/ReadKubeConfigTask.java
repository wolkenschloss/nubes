package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.libvirt.LibvirtException;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public abstract class ReadKubeConfigTask extends DefaultTask {

    @Input
    abstract public Property<String> getDomainName();

    @InputFile
    abstract public RegularFileProperty getKnownHostsFile();

    @OutputFile
    abstract public RegularFileProperty getKubeConfigFile();

    @Inject
    abstract public ExecOperations getExecOperations();

    @TaskAction
    public void read() throws IOException, LibvirtException, InterruptedException {

        var domain = new Domain(getDomainName().get());
        var ip = domain.getTestbedHostAddress();

        try (var stdout = new ByteArrayOutputStream()) {
            getExecOperations().exec(e -> {
                e.commandLine("ssh");
                e.args("-o", String.format("UserKnownHostsFile=%s", getKnownHostsFile().get().getAsFile().getAbsolutePath()),
                        ip, "microk8s", "config");
                e.setStandardOutput(stdout);
            }).assertNormalExitValue();

            var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
            var attributes = PosixFilePermissions.asFileAttribute(permissions);
            var path = getKubeConfigFile().get().getAsFile().toPath();
            var file = Files.createFile(path, attributes);

            Files.writeString(
                    file.toAbsolutePath(),
                    stdout.toString(),
                    StandardOpenOption.WRITE);
        }
    }
}
