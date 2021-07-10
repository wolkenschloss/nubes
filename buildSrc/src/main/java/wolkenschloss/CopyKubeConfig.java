package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import wolkenschloss.model.SecureShell;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
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

    @TaskAction
    public void read() throws Throwable {
        try (var testbed = new Testbed(getDomainName().get())) {
            var secureShell = testbed.getExec(getExecOperations(), getKnownHostsFile());
            var executor = secureShell.execute("microk8s", "config");
            executor.accept((SecureShell.Result result) -> {
                var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
                var attributes = PosixFilePermissions.asFileAttribute(permissions);
                var path = getKubeConfigFile().get().getAsFile().toPath();
                var file = Files.createFile(path, attributes);

                Files.writeString(
                        file.toAbsolutePath(),
                        result.getStdout(),
                        StandardOpenOption.WRITE);
            });
        }
    }
}
