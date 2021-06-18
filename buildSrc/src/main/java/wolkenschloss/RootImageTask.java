package wolkenschloss;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

abstract public class RootImageTask extends Exec {

    public RootImageTask() {
        commandLine("qemu-img");
    }

    @InputFile
    abstract public RegularFileProperty getBaseImage();

    @OutputFile
    abstract public RegularFileProperty getRootImage();

    @TaskAction
    @Override
    public void exec() {

        args("create", "-f" , "qcow2", "-F", "qcow2", "-b",
                getBaseImage().get(),
                getRootImage().get());

        super.exec();
    }
}
