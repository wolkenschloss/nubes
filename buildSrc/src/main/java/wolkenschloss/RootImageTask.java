package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.Incremental;

abstract public class RootImageTask extends Exec {

    public RootImageTask() {
        commandLine("qemu-img");
    }

    @InputFile
    @Incremental
    abstract public RegularFileProperty getBaseImage();

    @OutputFile
    abstract public RegularFileProperty getRootImage();

    @TaskAction
    @Override
    public void exec() {

        // Wenn es mal wieder Probleme mit Permission Denied gibt, wenn die
        // VM startet soll: Einfach mal appamor für diese VM ausschalten:
        // https://github.com/milkey-mouse/backup-vm/issues/17#issuecomment-527239052
        args("create", "-f" , "qcow2", "-F", "qcow2", "-b",
                getBaseImage().get(),
                getRootImage().get());

        super.exec();
    }
}
