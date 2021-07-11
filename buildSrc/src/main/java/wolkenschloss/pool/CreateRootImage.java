package wolkenschloss.pool;

import org.gradle.api.Action;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecSpec;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Erzeugt das Festplatten-Abbild für die virtuelle Maschine
 * des Prüfstandes.
 */
// Wenn es mal wieder Probleme mit Permission Denied gibt, wenn die
// VM startet soll: Einfach mal appamor für diese VM ausschalten:
@CacheableTask
abstract public class CreateRootImage extends DefaultTask {

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    abstract public RegularFileProperty getBaseImage();

    @Input
    abstract public Property<String> getSize();

    @Internal
    abstract public RegularFileProperty getRootImage();

    @OutputFile
    abstract public RegularFileProperty getRootImageMd5File();

    @Inject
    abstract public ExecOperations getExecOperations();

    @TaskAction
    public void exec() throws NoSuchAlgorithmException, IOException {

        boolean created = getRootImage().get().getAsFile().getParentFile().mkdirs();
        if (created) {
            getLogger().info("Directory created");
        }

        exec(spec -> spec.commandLine("qemu-img")
                .args("create", "-f", "qcow2", "-F", "qcow2", "-b",
                        getBaseImage().get(),
                        getRootImage().get()));

        exec(spec -> spec.commandLine("qemu-img")
                    .args("resize", getRootImage().get(), getSize().get()));

        var hash = getBaseImage().get().getAsFile().getAbsolutePath() + getSize().get();
        var md = MessageDigest.getInstance("MD5");

        md.update(hash.getBytes());
        Files.write(getRootImageMd5File().getAsFile().get().toPath(), md.digest());
    }

    private void exec(Action<? super ExecSpec> spec) throws IOException {
        try (var stdout = new ByteArrayOutputStream()) {
            var result = getExecOperations().exec(s -> {
                spec.execute(s);
                s.setStandardOutput(stdout);
            });
            getLogger().info(stdout.toString());
            result.assertNormalExitValue();
        }
    }
}

