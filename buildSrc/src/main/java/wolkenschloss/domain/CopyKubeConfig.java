package wolkenschloss.domain;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public abstract class CopyKubeConfig extends DefaultTask {

    @Input
    abstract public Property<String> getDomainName();

    @InputFile
    abstract public RegularFileProperty getKnownHostsFile();

    @OutputFile
    abstract public RegularFileProperty getKubeConfigFile();

    @Inject
    abstract public ExecOperations getExecOperations();

    @Internal
    abstract public Property<DomainOperations> getDomainOperations();

    @TaskAction
    public void read() throws Throwable {

        var domain = getDomainOperations().get();

        var shell = domain.getShell(getExecOperations());
        var command = shell.command("microk8s", "config");

        command.execute((SecureShellService.Result result) ->  {
            var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
            var attributes = PosixFilePermissions.asFileAttribute(permissions);
            var path = getKubeConfigFile().get().getAsFile().toPath();

            Path file;
            
            try {
                file = Files.createFile(path, attributes);
            } catch (IOException exception) {
                throw new RuntimeException("Can not create File", exception);
            }

            try {
                Files.writeString(
                        file.toAbsolutePath(),
                        result.getStdout(),
                        StandardOpenOption.WRITE);
            } catch (IOException exception) {
                throw new RuntimeException("Can not write File", exception);
            }
        });
    }
}
