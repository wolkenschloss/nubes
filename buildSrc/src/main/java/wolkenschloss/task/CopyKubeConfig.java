package wolkenschloss.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

// TODO: Refactor
import wolkenschloss.Testbed;
import wolkenschloss.TestbedExtension;

// Diese Beziehung ist OK
import wolkenschloss.task.start.Start;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public abstract class CopyKubeConfig extends DefaultTask {

    public static final String DEFAULT_KUBE_CONFIG_FILE_NAME = "kubeconfig";

    @Input
    abstract public Property<String> getDomainName();

    @InputFile
    abstract public RegularFileProperty getKnownHostsFile();

    @OutputFile
    abstract public RegularFileProperty getKubeConfigFile();

    @Inject
    abstract public ExecOperations getExecOperations();

    public void initialize(CopyKubeConfigParameter parameter, TestbedExtension extension, TaskProvider<Start> startDomain) {
        getDomainName().convention(extension.getDomain().getName());
        getKubeConfigFile().convention(extension.getRunDirectory().file(DEFAULT_KUBE_CONFIG_FILE_NAME));
        getKnownHostsFile().convention(startDomain.get().getKnownHostsFile());
    }

    @TaskAction
    public void read() throws Throwable {
        try (var testbed = new Testbed(getDomainName().get())) {
            var config = testbed.withDomain(domain -> {
                var ip = domain.getTestbedHostAddress();

                try (var stdout = new ByteArrayOutputStream()) {
                    getExecOperations().exec(e -> {
                        e.commandLine("ssh");
                        e.args("-o", String.format("UserKnownHostsFile=%s", getKnownHostsFile().get().getAsFile().getAbsolutePath()),
                                ip, "microk8s", "config");
                        e.setStandardOutput(stdout);
                    }).assertNormalExitValue();

                    return stdout.toString();
                }
            });

            var permissions = Set.of(PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_READ);
            var attributes = PosixFilePermissions.asFileAttribute(permissions);
            var path = getKubeConfigFile().get().getAsFile().toPath();
            var file = Files.createFile(path, attributes);

            Files.writeString(
                    file.toAbsolutePath(),
                    config,
                    StandardOpenOption.WRITE);
        }
    }
}
