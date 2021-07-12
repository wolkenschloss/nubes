package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import wolkenschloss.domain.SecureShellService;

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
    abstract public Property<SecureShellService> getSecureShellService();

    @TaskAction
    public void read() throws Throwable {

        var secureShell = getSecureShellService().get();
        var executor = secureShell.execute(
                getExecOperations(),
                "microk8s", "config");

        executor.accept((SecureShellService.Result result) -> {
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
