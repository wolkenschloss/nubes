package wolkenschloss;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;

abstract public class ResizeTask extends DefaultTask {

    @InputFile
    abstract public RegularFileProperty getImage();

    @Input
    abstract public Property<String> getSize();

    @Inject
    abstract public ExecOperations getExecOperations();

    @TaskAction
    public void exec() {
        var stdout = new ByteArrayOutputStream();
        var result = getExecOperations().exec(exec -> {
            exec.commandLine("qemu-img");
            exec.args("resize", getImage().get(), getSize().get());
            exec.setStandardOutput(stdout);
        });

        getLogger().info(stdout.toString());

        if (result.getExitValue() != 0) {
            throw new GradleException(String.format("qemu-image resize returns code %d", result.getExitValue()));
        }
    }
}
