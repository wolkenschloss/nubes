package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.process.ExecOperations;
import org.gradle.work.Incremental;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

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

    @OutputFile
    abstract public RegularFileProperty getRootImage();

    @Inject
    abstract public ExecOperations getExecOperations();

    @TaskAction
    public void exec() {


        getExecOperations().exec(spec -> {
            spec.commandLine("qemu-img");

            spec.args("create", "-f" , "qcow2", "-F", "qcow2", "-b",
                    getBaseImage().get(),
                    getRootImage().get());
        }).assertNormalExitValue();

        getExecOperations().exec(exec -> {
            exec.commandLine("qemu-img");
            exec.args("resize", getRootImage().get(), getSize().get());
        }).assertNormalExitValue();
    }
}
