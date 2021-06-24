package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
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
abstract public class RootImageTask extends DefaultTask {

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

        var hash = getBaseImage().get().getAsFile().getAbsolutePath() + getSize().get();

        var md = MessageDigest.getInstance("MD5");
        md.update(hash.getBytes());

        Files.write(getRootImageMd5File().getAsFile().get().toPath(), md.digest());

        getExecOperations().exec(spec -> {
            spec.commandLine("qemu-img")
                    .args("create", "-f", "qcow2", "-F", "qcow2", "-b",
                            getBaseImage().get(),
                            getRootImage().get());
        }).assertNormalExitValue();

        getExecOperations().exec(exec -> {
            exec.commandLine("qemu-img")
                    .args("resize", getRootImage().get(), getSize().get());
        }).assertNormalExitValue();
    }
}
